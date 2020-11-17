CREATE TABLE public.WORKER (
  workerId character varying(255) NOT NULL,
  script text NOT NULL,
  workerStatus character varying(255) NOT NULL,
  lastUpdateStateDate timestamp without time zone NOT NULL,
  createdAt timestamp without time zone,
  container jsonb,
  zoneOffset character varying(255) NOT NULL,
  stdOut text,
  stdErr text,
  CONSTRAINT worker_pkey PRIMARY KEY (workerId)
)
