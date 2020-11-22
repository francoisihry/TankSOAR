CREATE TABLE public.WORKER (
  workerId character varying(255) NOT NULL,
  script text NOT NULL,
  createdAt timestamp without time zone NOT NULL,
  zoneOffset character varying(255) NOT NULL,
  CONSTRAINT worker_pkey PRIMARY KEY (workerId)
);

CREATE TABLE public.DOCKER_STATE_SNAPSHOT (
  workerId character varying(255) NOT NULL,
  container jsonb NOT NULL,
  logStreams jsonb,
  snapshotDate timestamp without time zone NOT NULL,
  zoneOffset character varying(255) NOT NULL,
  CONSTRAINT docker_state_snapshot_pkey PRIMARY KEY (workerId, snapshotDate)
);