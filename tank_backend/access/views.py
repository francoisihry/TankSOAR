import json
from django.contrib.auth import authenticate, login, logout
from django.views.decorators.http import require_POST, require_GET
from django.views.decorators.csrf import ensure_csrf_cookie
from django.http import JsonResponse
import access.models
# from rest_framework.views import APIView
# from rest_framework.authentication import SessionAuthentication
# from rest_framework.response import Response


from rest_framework import viewsets
from rest_framework.authentication import SessionAuthentication
from rest_framework.permissions import IsAuthenticated

from .models import TankUser
from .serializers import UserSerializer, SettingsSerializer
from rest_framework.response import Response
from rest_framework import permissions
# from rest_framework_simplejwt.views import TokenObtainPairView
from django.contrib.auth.signals import user_logged_in
from rest_framework.views import APIView



class IsAdmin(permissions.BasePermission):
    def has_permission(self, request, view):
        """
        We check that the current user is an admin.
        """
        return request.user.is_admin


class CanEditUser(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        """
        If the user is the superadmin (ie username=admin), he can edit any admin user.
        Else we check that the current user is an admin.
        The obj to be modified is a TankUser.
        We check that the object to be modified is either the current user or not an admin.
        he superadmin user cannot have its username or roles edited.
        """
        if request.user.is_super_admin:
            if obj.is_super_admin:
                # prevent from destroying superadmin
                if view.action is 'destroy':
                    return False
                return not ('username' in request.data.keys() or 'roles' in request.data.keys())
            else:
                return True
        else:
            obj_is_current_user = request.user == obj
            return request.user.is_admin and (obj_is_current_user or not obj.is_admin)


class UserViewSet(viewsets.ModelViewSet):
    """
    Url: /api/users/
    """
    queryset = TankUser.objects.all()
    serializer_class = UserSerializer
    permission_classes = [IsAuthenticated, IsAdmin, CanEditUser]
    lookup_field = 'username'
    lookup_value_regex = '[\\w.@]+'  # regex to match any username in the url


class Settings(APIView):
    def patch(self, request):
        """
        Url: /api/me/settings/
        Edit the logged user settings.
        """
        settings = request.user.settings
        serializer = SettingsSerializer(data=request.data)
        serializer.is_valid()
        serializer.update(settings, request.data)
        return Response(serializer.data)


class Me(APIView):
    """
    Get the logged in user
    Url: /api/me/
    """
    # permission_classes = [IsAuthenticated]

    def get(self, request):
        """
        Return the logged user settings.
        """
        user = request.user
        serializer = UserSerializer(user)
        return Response(serializer.data)

# Authentication methods :
@ensure_csrf_cookie
def set_csrf_token(request):
    """
    Url :`/api/set-csrf-cookie/`
    """
    return JsonResponse({"details": "CSRF cookie set"})


@require_POST
def login_view(request):
    """
    Url : `/api/login/`
    """
    data = json.loads(request.body)
    username = data['username']
    password = data['password']
    if username is None or password is None:
        return JsonResponse({
            "errors": {
                "__all__": "Please enter both username and password"
            }
        }, status=400)

    user = authenticate(username=username, password=password)

    if user is not None:
        login(request, user)
        user_logged_in.send(sender=user.__class__, request=request, user=user)
        serializer = UserSerializer(user)
        return JsonResponse(serializer.data)
    return JsonResponse(
        {"detail": "Invalid credentials"},
        status=400,
    )

@require_GET
def logout_view(request):
    """
    Url : `/api/logout/`
    """
    logout(request)
    return JsonResponse({"details": "Logged out"})
