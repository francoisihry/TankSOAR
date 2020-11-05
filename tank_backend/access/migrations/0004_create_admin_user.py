from django.db import migrations
from django.contrib.auth import get_user_model


def create_roles(apps, schema_editor):
    Role = apps.get_model('access', 'Role')
    for (r_id, r_txt) in Role.id.field.choices:
        r_obj = Role.objects.create(id=r_id)
        r_obj.save()


def create_super_admin(apps, schema_editor):
    UserModel = get_user_model()
    super_admin_user = UserModel.objects.create_superuser(username='admin', password='TankAdmin!')
    super_admin_user.save()


class Migration(migrations.Migration):

    dependencies = [
        ('access', '0003_auto_20201104_1719'),
    ]

    operations = [
        migrations.RunPython(create_roles),
        migrations.RunPython(create_super_admin),
    ]
