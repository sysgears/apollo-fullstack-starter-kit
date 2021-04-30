package com.sysgears.service;

public interface MessageResolver {

	String getLocalisedMessage(String code, Object... interpolationArguments);
}
