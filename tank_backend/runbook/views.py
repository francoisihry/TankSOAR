from django.shortcuts import render

# Create your views here.
from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticated, BasePermission

from runbook.models import Runbook
from runbook.serializers import RunbookSerializer

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


class RunbookViewSet(viewsets.ModelViewSet):
    """
    Url: /api/runbooks/
    """
    lookup_value_regex = r'[\w.@-]+' # regex to match any name in the url

    queryset = Runbook.objects.all()
    serializer_class = RunbookSerializer
    permission_classes = [IsAuthenticated,CanEditRunbookOrReadOnly]
    lookup_field = 'name'
