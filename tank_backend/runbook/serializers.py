from rest_framework import serializers

from runbook.models import Runbook


class RunbookSerializer(serializers.ModelSerializer):

    class Meta:
        model = Runbook
        fields = '__all__'
