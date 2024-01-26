package com.bookmap.api.rpc.server.data.utils.modules;

import com.bookmap.api.rpc.server.data.utils.IncomeConverterManager;
import com.bookmap.api.rpc.server.data.income.converters.modules.*;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(
		modules = {
				AddPointToIndicatorEventConverterModule.class,
				AddFieldEventConverterModule.class,
				CancelOrderConverterModule.class,
				ClientInitConvertModule.class,
				ClientOffEventConverterModule.class,
				InitializationFinishedConverterModule.class,
				MoveOrderConverterModule.class,
				MoveOrderToMarketConverterModule.class,
				RegisterIndicatorConverterModule.class,
				ReqDataConverterModule.class,
				ResizeOrderConverterModule.class,
				SendOrderConverterModule.class,
				SubscribeToIndicatorConverterModule.class
		}
)
public interface IncomeConverterManagerFactory {
	IncomeConverterManager incomeConverterManager();
}
