package com.sysgears.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultMessageResolver implements MessageResolver {
	private final MessageSource messageSource;

	@Override
	public String getLocalisedMessage(String code, Object... interpolationArguments) {
		return messageSource.getMessage(code, interpolationArguments, LocaleContextHolder.getLocale());
	}
}
