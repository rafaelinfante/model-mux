package net.rafaelinfante.modelmux.gateway.compare;

import java.util.List;

public record CompareResponse(String prompt, List<CompareArm> arms) {}
