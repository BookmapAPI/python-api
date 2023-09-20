package com.bookmap.api.rpc.server.handlers;

import com.bookmap.api.rpc.server.data.income.*;
import com.bookmap.api.rpc.server.data.utils.AbstractEvent;
import com.bookmap.api.rpc.server.handlers.indicators.AddPointIndicatorHandler;
import com.bookmap.api.rpc.server.handlers.indicators.RegisterIndicatorHandler;

/**
 * Decides what handler should process a specified event.
 */
public class HandlerManager implements Handler<AbstractEvent> {

	private final SendingEventToClientHandler sendingEventToClientHandler;
	private final ReqDataHandler reqDataHandler;
	private final ClientInitHandler clientInitHandler;
	private final RegisterIndicatorHandler registerIndicatorHandler;
	private final AddPointIndicatorHandler addPointIndicatorHandler;
	private final FinishedInitializationHandler finishedInitializationHandler;
	private final ClientOffHandler clientOffHandler;
	private final AddUiFieldHandler addUiFieldHandler;
	private final SendOrderHandler sendOrderHandler;
	private final UpdateOrderHandler updateOrderHandler;

	public HandlerManager(SendingEventToClientHandler sendingEventToClientHandler, ReqDataHandler reqDataHandler,
						  ClientInitHandler clientInitHandler, RegisterIndicatorHandler registerIndicatorHandler,
						  AddPointIndicatorHandler addPointIndicatorHandler,
						  FinishedInitializationHandler finishedInitializationHandler,
						  ClientOffHandler clientOffHandler, AddUiFieldHandler addUiFieldHandler,
						  SendOrderHandler sendOrderHandler, UpdateOrderHandler updateOrderHandler) {
		this.sendingEventToClientHandler = sendingEventToClientHandler;
		this.reqDataHandler = reqDataHandler;
		this.clientInitHandler = clientInitHandler;
		this.registerIndicatorHandler = registerIndicatorHandler;
		this.addPointIndicatorHandler = addPointIndicatorHandler;
		this.finishedInitializationHandler = finishedInitializationHandler;
		this.clientOffHandler = clientOffHandler;
		this.addUiFieldHandler = addUiFieldHandler;
		this.sendOrderHandler = sendOrderHandler;
		this.updateOrderHandler = updateOrderHandler;
	}

	@Override
	public void handle(AbstractEvent event) {
		switch (event.type) {
			case ADD_POINT_TO_INDICATOR -> this.addPointIndicatorHandler.handle((AddPointToIndicatorEvent) event);
			case REQ_DATA -> this.reqDataHandler.handle((ReqDataEvent) event);
			case CLIENT_INIT -> this.clientInitHandler.handle((ClientInitEvent) event);
			case REGISTER_INDICATOR -> this.registerIndicatorHandler.handle((RegisterIndicatorEvent) event);
			case INITIALIZATION_FINISHED -> this.finishedInitializationHandler.handle((InitializationFinishedEvent) event);
			case ADD_SETTING_FIELD -> this.addUiFieldHandler.handle((AddUiField) event);
			case CLIENT_OFF -> this.clientOffHandler.handle((ClientOffEvent) event);
			case SEND_ORDER -> this.sendOrderHandler.handle((SendOrderEvent) event);
			case CANCEL_ORDER, MOVE_ORDER, MOVE_ORDER_TO_MARKET, RESIZE_ORDER -> {
				this.updateOrderHandler.handle((UpdateOrderEvent) event);
			}
			default -> this.sendingEventToClientHandler.handle(event);
		}
	}
}
