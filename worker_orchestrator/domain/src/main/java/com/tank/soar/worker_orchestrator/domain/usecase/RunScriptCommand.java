package com.tank.soar.worker_orchestrator.domain.usecase;

import java.util.Objects;

public final class RunScriptCommand implements UseCaseCommand {

    private final String script;

    private RunScriptCommand(final Builder builder) {
        this.script = Objects.requireNonNull(builder.script);
    }

    public static final Builder newBuilder() {
        return new Builder();
    }

    public String script() {
        return script;
    }

    public static class Builder {
        private String script;

        public Builder withScript(final String script) {
            this.script = script;
            return this;
        }

        public RunScriptCommand build() {
            return new RunScriptCommand(this);
        }

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RunScriptCommand)) return false;
        RunScriptCommand that = (RunScriptCommand) o;
        return Objects.equals(script, that.script);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script);
    }

    @Override
    public String toString() {
        return "RunScriptCommand{" +
                "script='" + script + '\'' +
                '}';
    }
}
