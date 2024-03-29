## Docker

### Use tcp to connect with the docker daemon

We need to change how the docker daemon is launched by adding this argument *-H tcp://localhost* 

> #Under Centos8
> **systemctl status docker** will tell you if the service is active or not

So now we will update the docker service to activate the listening using tcp.

1. Edit systemctl by doing **sudo systemctl edit --full docker**
1. Change this **ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock** by adding **-H tcp://localhost**
> You should get **ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock -H tcp://localhost**
1. Restart the service **systemctl restart docker**
1. Check the service has been restarted and updated by doing **system status docker**
1. Now you should be able to list all containers by doing **docker -H tcp://0.0.0.0:2375 ps**

## Worker Orchestrator

Build it by launching the script **buid_worker_orchestrator.sh**.

### Docker

We cannot use the docker socket as we are stuck about permission. The container need to be in the docker group but it is not easily faisable.
Or we could run the container as root but the image used to build the application prevents it.
The only remaining way is to use tcp to communicate with docker daemon.

Please see the previous paragraph on how to setup **tcp**.

### SWAGGER

Launch all services by doing *docker-compose up*

Access it using this url in your browser: http://localhost:8080/swagger-ui/#/default