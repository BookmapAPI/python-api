package com.bookmap.api.rpc.server.addon;

import com.bookmap.api.rpc.server.*;
import com.bookmap.api.rpc.server.data.outcome.InstrumentDetachedEvent;
import com.bookmap.api.rpc.server.data.outcome.InstrumentInfoEvent;
import com.bookmap.api.rpc.server.data.outcome.OnIntervalEvent;
import com.bookmap.api.rpc.server.data.outcome.ServerOffEvent;
import com.bookmap.api.rpc.server.exceptions.FatalServerException;
import com.bookmap.api.rpc.server.log.PythonStackTraceTracker;
import com.bookmap.api.rpc.server.log.RpcLogger;
import velox.api.layer1.annotations.*;
import velox.api.layer1.common.DirectoryResolver;
import velox.api.layer1.common.Log;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.simplified.*;
import velox.gui.StrategyPanel;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.stream.Stream;

@Layer1SimpleAttachable
@Layer1StrategyName("RPC addon")
@NoAutosubscription
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
@Layer1TradingStrategy
public class RpcServerAddon
		implements
		CustomModule,
		IntervalListener,
		CustomSettingsPanelProvider {

	private static final String FILE_SCRIPT_NAME = "script.py";
	private static final ConcurrentHashMap<String, State> ALIAS_TO_STATE = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, CompletableFuture<?>> ALIAS_TO_INITIALIZATION_TASK = new ConcurrentHashMap<>();
	private static final Object INIT_LOCK = new Object();
	private static final String ADDON_RUNTIME_DIRECTORY_NAME = RpcServerAddon.class.getSimpleName(); // modified during addon building
	private static final Path PYTHON_DIR = DirectoryResolver.getBookmapDirectoryByName("Python");
	private static final Path PYTHON_SCRIPT_RUNTIME_DIR = PYTHON_DIR.resolve("tmp").resolve(ADDON_RUNTIME_DIRECTORY_NAME);
	private static final File SCRIPT_FILE = PYTHON_SCRIPT_RUNTIME_DIR.resolve(FILE_SCRIPT_NAME).toFile();
	public static final int PYTHON_ERROR_WIDTH = 800;
	private static Instance instance;
	private static ExecutorService executor;

	static {
		try {
			Files.createDirectories(PYTHON_SCRIPT_RUNTIME_DIR);
		} catch (IOException e) {
			throw new FatalServerException("Can't create tmp directory under python lib, check access of Bookmap to " + PYTHON_DIR.toAbsolutePath());
		}
		try {
			Log.info("Create lib " + PYTHON_SCRIPT_RUNTIME_DIR);
			// unpack jar file to tmp folder to be able to build
			PackagingUtils.unpackSpecificResourceFilesFromTheCurrentJar(FILE_SCRIPT_NAME, PYTHON_SCRIPT_RUNTIME_DIR);
			// unpack runtime environment
			PackagingUtils.unpackSpecificResourceFilesFromTheCurrentJar("bookmap", PYTHON_SCRIPT_RUNTIME_DIR);
		} catch (IOException e) {
			throw new FatalServerException(e);
		}
	}

	private String alias;
	private State initialState;

	@Override
	public void initialize(String alias, InstrumentInfo instrumentInfo, Api api, InitialState initialState) {
		synchronized (INIT_LOCK) {
			this.alias = alias;
			this.initialState = new State(instrumentInfo, api, initialState);
			ALIAS_TO_STATE.put(alias, this.initialState);

			var future = new CompletableFuture<>();

			executor = Executors.newSingleThreadExecutor();
			CompletableFuture<Integer> pythonExitCode = new CompletableFuture<>();
			executor.execute(() -> {
				try {
					int exitCode = pythonExitCode.get();
					if (exitCode != 0) {
						String pythonStacktrace = PythonStackTraceTracker.getTracker().get();
						String[] lines = pythonStacktrace.split("\n");

						// Create a StringBuilder to build the formatted HTML string
						// We need to wrap every single line, we can't just wrap all stacktrace with <pre> tag
						// because it will work only for the first line in stacktrace
						StringBuilder finalPythonStacktrace = new StringBuilder("<html><body>");
						for (String line : lines) {
							finalPythonStacktrace.append("<pre style='width:" + PYTHON_ERROR_WIDTH + "px; font-size: 13px;'>")
									.append(line)
									.append("</pre>");
						}

						SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,
								"Python script looks failed, check logs and try " +
										"to rebuild addon. Current instance will be stopped. Please detach addon from Bookmap.\n" +
										"Python stacktrace (last 30 lines):\n\n" + finalPythonStacktrace,
								"Python Error", JOptionPane.ERROR_MESSAGE));
						instance = null;
						throw new RuntimeException(pythonStacktrace);
					}
				} catch (ExecutionException | InterruptedException e) {
					RpcLogger.warn("Monitoring python exit code task interrupted", e);
				}
			});

			ALIAS_TO_INITIALIZATION_TASK.put(alias, future);

			if (instance == null) {
				instance = new ExternalProcessInstance(SCRIPT_FILE, ALIAS_TO_STATE, ALIAS_TO_INITIALIZATION_TASK, pythonExitCode);
				instance.run();
			}
			this.initialState.instrumentApi.addIntervalListeners(this);

			// TODO: find a better place to send this
			instance.getEventLoop().pushEvent(
					new InstrumentInfoEvent(
							instrumentInfo.pips,
							instrumentInfo.sizeMultiplier,
							instrumentInfo.multiplier,
							instrumentInfo.isCrypto,
							instrumentInfo.fullName,
							alias)
			);
			try {
				// Blocking here is done because not all functionality can be done outside of initialize method
				// TODO: we should avoid such unnecessary limitations, this addon shall be rewritten using purely the Core API.
				future.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException("Error during initialization", e);
			} catch (CancellationException e) {
				RpcLogger.warn("Init task was cancelled");
			}
			ALIAS_TO_INITIALIZATION_TASK.remove(alias);
		}
	}

	@Override
	public void stop() {
		synchronized (INIT_LOCK) {
			if (instance != null && instance.isRun()) {
				ALIAS_TO_STATE.computeIfPresent(alias, (k, v) -> {
					instance.getEventLoop().pushEvent(new InstrumentDetachedEvent(alias));
					return null;
				});
				ALIAS_TO_INITIALIZATION_TASK.computeIfPresent(alias, (k, v) -> {
					v.complete(null);
					return null;
				});
				if (ALIAS_TO_STATE.isEmpty()) {
					instance.getEventLoop().pushEvent(new ServerOffEvent());
					instance.stop();
				}
			}
			if (executor != null) {
				executor.shutdownNow();
				RpcLogger.info("Executor stopped");
				executor = null;
			}
			instance = null;
		}
	}

	@Override
	public long getInterval() {
		// TODO: manually injected interval listener does not seem to be working, but worth to test carefully.
		// TODO: can we change this interval dynamically?
		// TODO: should we allow users to configure it?
		return Intervals.INTERVAL_100_MILLISECONDS;
	}

	@Override
	public void onInterval() {
		if (instance != null && instance.getEventLoop() != null) {
			instance.getEventLoop().pushEvent(new OnIntervalEvent(alias));
		}
	}

	@Override
	public StrategyPanel[] getCustomSettingsPanels() {
		var colorSettings = initialState.colorsConfig;
		var defaultSettings = initialState.settingsConfig;
		var panels = Stream.empty();
		// color settings does not have any components unless color item was added, so no need to show it
		if (colorSettings != null && colorSettings.getComponentCount() > 0) {
			panels = Stream.concat(panels, Stream.of(colorSettings));
		}
		// common setting initially contains button to save changes,
		// so unless there is nothing except button, it should be ignored
		if (defaultSettings != null && defaultSettings.getComponentCount() > 1) {
			panels = Stream.concat(panels, Stream.of(defaultSettings));
		}

		return panels.map(panel -> (StrategyPanel) panel).toArray(StrategyPanel[]::new);
	}
}
