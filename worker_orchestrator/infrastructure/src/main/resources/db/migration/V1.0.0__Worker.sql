CREATE TABLE public.WORKER (
  workerId character varying(255) NOT NULL,
  script text NOT NULL,
  CONSTRAINT worker_pkey PRIMARY KEY (workerId)
);

CREATE TABLE public.WORKER_EVENT (
  workerId character varying(255) NOT NULL,
  eventType character varying(255) NOT NULL,-- clé : USER, DOCKER
  eventDate timestamp without time zone NOT NULL,
  zoneOffset character varying(255) NOT NULL,
--  container
  container jsonb,
  logStreams jsonb,
--  action manuel
  userEventType character varying(255),-- CREATION_REQUESTED
  CONSTRAINT worker_event_pkey PRIMARY KEY (workerId, eventDate)
);