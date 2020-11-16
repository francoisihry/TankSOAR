from rest_framework import serializers

from runbook.models import Runbook, Worker


class WorkerSerializer(serializers.ModelSerializer):

    class Meta:
        model = Worker
        fields = '__all__'


class RunbookSerializer(serializers.ModelSerializer):
    worker = WorkerSerializer(label='Settings', required=False)

    class Meta:
        model = Runbook
        fields = '__all__'