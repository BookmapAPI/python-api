package com.bookmap.api.rpc.server.data.utils.modules;

import com.bookmap.api.rpc.server.data.utils.OutcomeConverterManager;
import com.bookmap.api.rpc.server.data.outcome.converters.modules.*;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(
		modules = {
				BalanceConverterModule.class,
				DepthDataConverterModule.class,
				ErrorEventConverterModule.class,
				IndicatorResponseConverterModule.class,
				InstrumentDetachedConverterModule.class,
				InstrumentInfoConverterModule.class,
				MboDataEventConverterModule.class,
				OnIntervalConverterModule.class,
				OnSettingsParameterChangedConverterModule.class,
				OrderExecutionConverterModule.class,
				OrderUpdateConverterModule.class,
				PositionUpdateConverterModule.class,
				RespDataConverterModule.class,
				ServerInitConverterModule.class,
				ServerOffConverterModule.class,
				TradeDataConverterModule.class
		}
)
public interface OutcomeConverterManagerFactory {
	OutcomeConverterManager outcomeConverterManager();
}
