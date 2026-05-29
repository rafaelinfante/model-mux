package net.rafaelinfante.modelmux.skills.teststub;

import jakarta.validation.constraints.NotBlank;

public record TestStubRequest(@NotBlank String javaSource) {}
