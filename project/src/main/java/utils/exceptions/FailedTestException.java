package utils.exceptions;

import utils.log.CommonLogMessages;

public class FailedTestException extends RuntimeException implements CommonLogMessages {
	private static final String INTERRUPTED_TEST_MESSAGE
			= "Test execution was interrupted with reason:\n - ";
	private static final String INTERRUPTED_BY_EXCEPTION_TEST_MESSAGE
			= "Test execution was interrupted by exception with reason:\n - ";
	public FailedTestException(String message, String... parameters){
		super(INTERRUPTED_TEST_MESSAGE + "[" + CommonLogMessages.formatMessage(message, parameters) + "]");
	}

	public FailedTestException(Throwable t, String message, String... parameters){
		super(INTERRUPTED_BY_EXCEPTION_TEST_MESSAGE + "[" + CommonLogMessages.formatMessage(message, parameters) + "]", t);
	}
}
