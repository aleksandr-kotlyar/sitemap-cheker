package com.qiwi.bonus;

import io.qameta.allure.Step;

import java.util.logging.Logger;

/**
 * Created by Aleksandr Kotlyar on 09.11.2017. 22:45
 */
public class StepsLogger {

    @Step
    protected void assertion(String assertion, Runnable r) {
        try {
            r.run();
            info("Assertion PASSED: " + assertion);
        } catch (Throwable t) {
            info("Assertion FAILED: " + assertion + "\n" + t);
            throw t;
        }
    }

    @Step
    protected void act(String act, Runnable r) {
        try {
            r.run();
            info("Act PASSED: " + act);
        } catch (Throwable t) {
            info("Act FAILED: " + act + "\n" + t);
            throw t;
        }
    }


    @Step
    protected void arrange(String arrange, Runnable r) {
        try {
            r.run();
            info("Arrange PASSED: " + arrange);
        } catch (Throwable t) {
            info("Arrange FAILED: " + arrange + "\n" + t);
            throw t;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(StepsLogger.class.getName());

    public void info(String logMessage) {
        getLoggerInstance().info(logMessage);
    }

    private Logger getLoggerInstance() {
        return LOGGER;
    }
}
