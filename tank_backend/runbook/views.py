import json

from django.http import JsonResponse
from django.shortcuts import render

# Create your views here.
from django.views.decorators.http import require_POST
from rest_framework import viewsets
from rest_framework.decorators import action
from rest_framework.permissions import IsAuthenticated, BasePermission

from .models import Runbook, Worker
from .serializers import RunbookSerializer
import requests
from .worker_interface import WorkerInterface

SAFE_METHODS = ['GET', 'HEAD', 'OPTIONS']


class CanEditRunbookOrReadOnly(BasePermission):
    """
    The request is authenticated as a user, or is a read-only request.
    """

    def has_permission(self, request, view):
        can_edit_runbook = request.user.is_developper or request.user.is_admin
        if (request.method in SAFE_METHODS or
                request.user and
                can_edit_runbook):
            return True
        return False

from .tasks import run_script

class RunbookViewSet(viewsets.ModelViewSet):
    """
    Url: /api/runbooks/
    """
    lookup_value_regex = r'[\w.@-]+' # regex to match any name in the url

    queryset = Runbook.objects.all()
    serializer_class = RunbookSerializer
    permission_classes = [IsAuthenticated,CanEditRunbookOrReadOnly]
    lookup_field = 'name'

    @action(detail=True, methods=['get'])
    def run(self, request, name):
        runbook = Runbook.objects.get(name=name)

        worker_id, stdout, stderr, status = WorkerInterface.create_worker(
            script_name=runbook.name,
            script_content=runbook.content
        )
        worker = runbook.worker
        worker.worker_id = worker_id
        worker.status = status
        worker.save()
        run_script.delay(worker_id=worker_id, pk=worker.pk)
        return JsonResponse({
            "worker_id": worker_id,
            "status": status
        })

    # @action(detail=True, methods=['get'])
    # def status(self, request, name):
    #     runbook = Runbook.objects.get(name=name)
    #     worker_id = runbook.worker_id
    #
    #     worker_id, status = WorkerInterface.get_worker(worker_id)
    #
    #     return 'ok'


WORKER_ORCHESTRATOR_URL = 'http://localhost/'



