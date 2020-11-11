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


class Worker(models.Model):
    id = models.IntegerField(primary_key=True)
    runbook = models.ForeignKey(Runbook, on_delete=models.CASCADE, blank=False)
    stdout = models.TextField(blank=True)
    stderr = models.TextField(blank=True)
    status = models.TextField(default=False)