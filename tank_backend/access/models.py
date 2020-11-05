from django.utils import timezone
from django.contrib.auth.models import AbstractUser, models, BaseUserManager, Group
from django.utils.translation import gettext_lazy as _


class Role(models.Model):
    """
    An admin can create other users and give them any role.
    And admin user cannot edit or delete another admin user.
    There is only one super_admin account (the admin user): he can edit and delete any user (including the admin users).
    The username of the 'admin' user cannot be changed.
    """
    ANALYST = 1
    DEVELOPER = 2
    ADMIN = 3
    ROLE_CHOICES = (
        (ANALYST, 'analyst'),
        (DEVELOPER, 'developer'),
        (ADMIN, 'admin'),
    )

    id = models.PositiveSmallIntegerField(choices=ROLE_CHOICES, primary_key=True)

    def __str__(self):
        return self.get_id_display()


class Settings(models.Model):
    """
     Define the custom settings of each user like the language, date format etc...
    """
    id = models.AutoField(primary_key=True)
    timezone = models.CharField(max_length=100, blank=False,
                                default='Europe/Paris')



class TankUserManager(BaseUserManager):
    """
        CUSTOM UserManager To handle user creation and superuser creation.
        INITIALLY 2 methods
        1- create_user() accepts all parameters which are REQUIRED_FIELDS
        2- create_superuser() to create a SuperUser
        **WARNING**: In create_user roles for specific user will be set after User has been saved to database and we have to use set(). Which accepts list of args eg: set([value])
    """

    def create_user(self, username,
                    roles=None, password=None, email=None):
        # check permissions
        # if Role.ADMIN not in [r.id for r in list(current_user.roles.all())]:
        #     raise PermissionDenied()

        # check args
        if not username:
            raise ValueError('Username must be set')
        if not password:
            raise ValueError('Password must be set')
        settings = Settings.objects.create()
        settings.save()
        user = self.model(
            username=username,
            settings=settings)
        user.set_password(password)
        user.save()
        roles_objs = []
        if roles is not None:
            roles_objs = list(Role.objects.filter(id__in=roles))
        user.roles.set(roles_objs)
        if email is not None:
            user.email = email
        user.save()
        return user

    def create_superuser(self, username, password, email=None):
        roles = [Role.ADMIN]
        user = self.create_user(
            username,
            roles=roles,
            password=password,
            email=email
        )
        return user


class TankUser(AbstractUser):
    # username = models.CharField(max_length=120)
    roles = models.ManyToManyField(Role)
    last_login = models.DateTimeField(_('last login'), default=timezone.now)
    settings = models.OneToOneField(Settings, on_delete=models.CASCADE, blank=False)

    objects = TankUserManager()

    USERNAME_FIELD = 'username'

    REQUIRED_FIELDS = ['password']

    def __str__(self):
        return self.username

    @property
    def is_developper(self):
        return Role.DEVELOPER in [r.id for r in list(self.roles.all())]

    @property
    def is_analyst(self):
        return Role.ANALYST in [r.id for r in list(self.roles.all())]

    @property
    def is_admin(self):
        return Role.ADMIN in [r.id for r in list(self.roles.all())]

    @property
    def is_super_admin(self):
        return self.username == 'admin'


    class Meta(AbstractUser.Meta):
        abstract = False

# class SuperAdminTankUser(AbstractUser):
#     readonly_fields = ('username',)
