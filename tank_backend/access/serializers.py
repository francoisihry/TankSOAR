from rest_framework.fields import EmailField, CharField, DateTimeField

from .models import TankUser, Role, Settings
from rest_framework import serializers


class RoleSerializer(serializers.Serializer):
    def update(self, instance, validated_data):
        pass

    def create(self, validated_data):
        pass

    id = serializers.IntegerField()


class SettingsSerializer(serializers.ModelSerializer):
    class Meta:
        model = Settings
        fields = '__all__'
        # fields = ['timezone']

    def update(self, instance, validated_data):
        instance.timezone = validated_data.get('timezone', instance.timezone)
        instance.save()
        return instance


class UserSerializer(serializers.HyperlinkedModelSerializer):
    roles = RoleSerializer(many=True, label='Role', default=None)
    email = EmailField(label='Email address', max_length=254, required=False, default=None)
    first_name = CharField(label='First name', max_length=254, required=False, default=None)
    last_name = CharField(label='Last name', max_length=254, required=False, default=None)
    last_login = DateTimeField(label='Last login', required=False, default=None)
    settings = SettingsSerializer(label='Settings', required=False)

    class Meta:
        model = TankUser
        fields = ['username', 'password', 'roles', 'email', 'first_name', 'last_login', 'last_name', 'settings']
        extra_kwargs = {'password': {'write_only': True}
                        }

    def create(self, validated_data):
        if validated_data['roles'] is not None:
            roles = [r['id'] for r in validated_data['roles']]
        else:
            roles = None

        user = TankUser.objects.create_user(
            username=validated_data['username'],
            password=validated_data['password'],
            email=validated_data['email'],
            roles=roles
        )
        return user

    def update(self, instance, validated_data):
        for k in validated_data.keys():
            if k == 'username':
                instance.username = validated_data['username']
                instance.save()
            if k == 'roles':
                id_list = [r['id'] for r in validated_data['roles']]
                roles_objs = list(Role.objects.filter(id__in=id_list))
                instance.roles.set(roles_objs)
            # if k == 'settings':
            #     serializer = SettingsSerializer(instance.settings)
            #     serializer.update(instance.settings, validated_data[k])

        return instance
