from django.db import models
from django.core.validators import RegexValidator


class Language(models.TextChoices):
    PYTHON = 'python', 'Python'
    JAVASCRIPT = 'js', 'JavaScript'
    SHELL = 'sh', 'Shell'


class RunbookNameValidator(RegexValidator):
    regex = r'^[\w.@-]+\Z'
    message = 'Enter a valid runbook name. This value may contain only letters,' \
              'numbers, and @/./-/_ characters.'
    code = 'invalid_runbook_name'


class WorkerStatus(models.TextChoices):
    READY = 'ready'
    RUNNING = 'running'
    FINISHED = 'finished'
    ERROR = 'error'


class Worker(models.Model):
    worker_id = models.IntegerField(blank=True, null=True)
    stdout = models.TextField(blank=True)
    stderr = models.TextField(blank=True)
    status = models.CharField(
        max_length=10,
        choices=WorkerStatus.choices,
        default=WorkerStatus.READY,
    )

    @classmethod
    def get_new(cls):
        return cls.objects.create().id


class Runbook(models.Model):
    name = models.CharField(max_length=120,
                            validators=[RunbookNameValidator()],
                            unique=True)
    content = models.TextField(blank=True)
    language = models.CharField(
        max_length=10,
        choices=Language.choices,
        default=Language.PYTHON,
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    worker = models.OneToOneField(Worker, on_delete=models.CASCADE, default=Worker.get_new)

