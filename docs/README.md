# docs

`model-mux.gif` (referenced from the top-level README) is a short screen capture of the playground.
Record it against `docker compose up` and drop the file in here. It should show:

- **Force failover** — flip the toggle and watch the request fall through to the next provider.
- **Compare** — the same prompt answered side by side, with tokens, cost, and latency per provider.
- **The live token / cost meter** updating as requests run.
