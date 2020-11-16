WORKER_ORCHESTRATOR_URL = 'http://localhost/'

class WorkerInterface:
    @staticmethod
    def create_worker(script_name, script_content):
        # result = requests.post(WORKER_ORCHESTRATOR_URL + 'workers',
        #                        data=json.dumps({
        #                            'script_name': script_name,
        #                            'script_content': script_content
        #                        }))
        worker_id = 42
        stdout = None
        stderr = None
        status = 'running'
        return worker_id, stdout, stderr, status

    @staticmethod
    def get_workers():
        # result = requests.get(WORKER_ORCHESTRATOR_URL + 'workers')
        return [
            (1, 'finished'),
            (2, 'running'),
            (42, 'running')
        ]

    @staticmethod
    def get_worker(worker_id):
        # result = requests.get(WORKER_ORCHESTRATOR_URL + 'workers/' + worker_id)
        return 42, 'finished'

    @staticmethod
    def get_stdout(worker_id):
        # result = requests.get(WORKER_ORCHESTRATOR_URL + 'workers/' + worker_id + '/stdout')
        return 'stdout...'

    @staticmethod
    def get_stderr(worker_id):
        # result = requests.get(WORKER_ORCHESTRATOR_URL + 'workers/' + worker_id + '/stderr')
        return 'stderr...'