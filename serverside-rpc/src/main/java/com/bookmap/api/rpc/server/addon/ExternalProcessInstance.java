package com.bookmap.api.rpc.server.addon;

import com.bookmap.api.rpc.server.EventLoop;
import com.bookmap.api.rpc.server.LogTracker;
import com.bookmap.api.rpc.server.State;
import com.bookmap.api.rpc.server.communication.DefaultMessageReader;
import com.bookmap.api.rpc.server.communication.LocalTcpSocketServer;
import com.bookmap.api.rpc.server.communication.MessageReader;
import com.bookmap.api.rpc.server.communication.Server;
import com.bookmap.api.rpc.server.data.outcome.ServerInitEvent;
import com.bookmap.api.rpc.server.data.utils.IncomeConverterManager;
import com.bookmap.api.rpc.server.data.utils.OutcomeConverterManager;
import com.bookmap.api.rpc.server.data.utils.modules.DaggerIncomeConverterManagerFactory;
import com.bookmap.api.rpc.server.data.utils.modules.DaggerOutcomeConverterManagerFactory;
import com.bookmap.api.rpc.server.exceptions.FailedToStartServerException;
import com.bookmap.api.rpc.server.exceptions.FatalServerException;
import com.bookmap.api.rpc.server.handlers.*;
import com.bookmap.api.rpc.server.handlers.indicators.AddPointIndicatorHandler;
import com.bookmap.api.rpc.server.handlers.indicators.RegisterIndicatorHandler;
import com.bookmap.api.rpc.server.log.RpcLogger;
import velox.api.layer1.common.Log;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

public class ExternalProcessInstance implements Instance {

	private static final Object INSTANCE_STATE_CHECK_LOCK = new Object();
	private static ScheduledExecutorService HEALTH_CHECK_SERVICE;
	private static final int PERIODS_OF_HEALTH_CHECK_SECONDS = 3;

	private final ConcurrentMap<String, State> aliasToState;
	private final ConcurrentMap<String, CompletableFuture<?>> aliasToInitializationTask;
	private Server server;
	private final File scriptFile;
	private MessageReader reader;
	private EventLoop eventLoop;
	private ExecutorService service;
	private boolean isRun = false;
	private Process runningProcess;
	private final CompletableFuture<Integer> pythonExitCode;


	public ExternalProcessInstance(File scriptFile, ConcurrentMap<String, State> aliasToState, ConcurrentMap<String, CompletableFuture<?>> aliasToInitializationTask, CompletableFuture<Integer> pythonExitCode) {
		this.scriptFile = scriptFile;
		this.aliasToState = aliasToState;
		this.aliasToInitializationTask = aliasToInitializationTask;
		this.pythonExitCode = pythonExitCode;
		HEALTH_CHECK_SERVICE = Executors.newSingleThreadScheduledExecutor();

		// simple server health check. Since Python code can't be checked on correctness before run, we should periodically
		// check the communication channel state and report any issues to user to improve development experience.
		// Important to note, that this check is not ideal, it tracks only the state of the server and its communication with python,
		// Sometimes due to server bug or any other reasons, error might appear by server won't fail. In this case, health
		// TODO: here should be more complex entity of health checking taking into account closed streams, wrong messages received
		//  from Python etc.
		HEALTH_CHECK_SERVICE.scheduleWithFixedDelay(() -> {
			synchronized (INSTANCE_STATE_CHECK_LOCK) {
				if (!isRun) {
					return;
				}
				if (!server.isAlive() || runningProcess == null || !runningProcess.isAlive()) {
					RpcLogger.warn("Client looks dead, but it seems that Bookmap did not stop it explicitly. Check whether Python script is failed");

					RpcLogger.info("Size of init tasks " + aliasToInitializationTask.size());
					for (CompletableFuture<?> task : aliasToInitializationTask.values()) {
						Log.warn("Init task will be completed exceptionally");
						task.completeExceptionally(new FatalServerException("Python script looks failed"));
					}
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
							"Python script looks failed, check logs and try to rebuild addon. Current instance will be stopped. Please detach addon from Bookmap.")
					);
					RpcLogger.error("Instance will be stopped");
					stop();
				}
			}
		}, PERIODS_OF_HEALTH_CHECK_SECONDS, PERIODS_OF_HEALTH_CHECK_SECONDS, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			int port = Config.getTcpPort();
			ProcessBuilder processBuilder = new ProcessBuilder().command(Config.getPythonRuntime(), "-X utf8", scriptFile.getAbsolutePath(), String.valueOf(port));
			server = new LocalTcpSocketServer(port);
			runningProcess = processBuilder.start();
			service = Executors.newSingleThreadExecutor();
			LogTracker.initExecutorService();
			LogTracker.track(Log.LogLevel.INFO, runningProcess.getInputStream());
			LogTracker.track(Log.LogLevel.ERROR, runningProcess.getErrorStream());

			service.execute(() -> {
				try {
					int exitCode = runningProcess.waitFor();
					if (exitCode == 1) {
						stop();
						for (CompletableFuture<?> task : aliasToInitializationTask.values()) {
							RpcLogger.warn("Init task will be cancelled");
							task.cancel(true);
						}
					}
					pythonExitCode.complete(exitCode);
				} catch (InterruptedException e) {
					RpcLogger.warn("Monitoring exit code interrupted", e);
				}
			});

			synchronized (aliasToInitializationTask) {
				isRun = true;
			}
			Log.info("Processed started");
			server.start();
			eventLoop = new EventLoop();
			IncomeConverterManager incomeConverterManager = DaggerIncomeConverterManagerFactory.create().incomeConverterManager();
			OutcomeConverterManager outcomeConverterManager = DaggerOutcomeConverterManagerFactory.create().outcomeConverterManager();
			SendingEventToClientHandler sendingEventToClientHandler = new SendingEventToClientHandler(outcomeConverterManager, server);
			ReqDataHandler reqDataHandler = new ReqDataHandler(eventLoop, aliasToState);
			ClientInitHandler clientInitHandler = new ClientInitHandler();
			HandlerManager handlerManager = new HandlerManager(
					sendingEventToClientHandler, reqDataHandler, clientInitHandler,
					new RegisterIndicatorHandler(aliasToState, eventLoop), new AddPointIndicatorHandler(aliasToState, eventLoop), new FinishedInitializationHandler(aliasToInitializationTask),
					new ClientOffHandler(eventLoop), new AddUiFieldHandler(aliasToState, eventLoop),
					new SendOrderHandler(aliasToState, eventLoop), new UpdateOrderHandler(aliasToState, eventLoop));
			eventLoop.setHandlerManager(handlerManager);
			reader = new DefaultMessageReader(server, incomeConverterManager, eventLoop);
			eventLoop.pushEvent(new ServerInitEvent());
			reader.start();
			RpcLogger.info("Instance started");
		} catch (IOException | FailedToStartServerException e) {
			RpcLogger.error("Failed to run instance", e);
			throw new FatalServerException(e);
		}
	}

	@Override
	public void stop() {
		synchronized (INSTANCE_STATE_CHECK_LOCK) {
			try {
				if (runningProcess != null) {
					RpcLogger.info("Process is active, stopping...");
					runningProcess.destroyForcibly();
				}
				RpcLogger.info("Process stopped");
				if (reader != null) {
					reader.stop();
					RpcLogger.info("Reader stopped");
				}
				if (eventLoop != null) {
					eventLoop.close();
					RpcLogger.info("Event loop stopped");
					eventLoop = null;
				}
				if (service != null) {
					service.shutdownNow();
					RpcLogger.info("ExecutorService stopped");
					service = null;
				}
				LogTracker.finish();
				RpcLogger.info("Log tracker for Python finished");
				HEALTH_CHECK_SERVICE.shutdownNow();
				RpcLogger.info("Health check service stopped");
				server.close();
				Log.info("Server stopped");
				runningProcess = null;
				RpcLogger.info("Instance has been stopped");
			} catch (IOException e) {
				RpcLogger.error("Failed to stop instance", e);
				throw new FatalServerException(e);
			} finally {
				isRun = false;
			}
		}
	}

	@Override
	public EventLoop getEventLoop() {
		return eventLoop;
	}

	@Override
	public boolean isRun() {
		return isRun;
	}
}
