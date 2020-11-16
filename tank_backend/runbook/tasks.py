from celery import shared_task
from .worker_interface import WorkerInterface
from .models import Worker

@shared_task
def run_script(worker_id, pk):
    _, status = WorkerInterface.get_worker(worker_id)
    stdout = WorkerInterface.get_stdout(worker_id)
    stderr = WorkerInterface.get_stderr(worker_id)
    print('worker_id : {}'.format(worker_id))
    print('status : {}'.format(status))
    print('stdout : {}'.format(stdout))
    print('stderr : {}'.format(stderr))
    worker = Worker.objects.get(pk=pk)
    worker.stdout = stdout
    worker.stderr = stderr
    worker.status = status
    worker.save()
    return '{} Run!'.format(worker_id)