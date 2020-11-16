from django.contrib import admin
from django.urls import path, include
from rest_framework import routers
from access.views import UserViewSet, Me, Settings, set_csrf_token, login_view, logout_view
from runbook.views import RunbookViewSet

from rest_framework.schemas import get_schema_view


router = routers.DefaultRouter()
router.register(r'users', UserViewSet, basename='user')
router.register(r'runbooks', RunbookViewSet, basename='runbook')

urlpatterns = [
    path('api/', include(router.urls)),
    path('api/me/', Me.as_view()),
    path('api/me/settings/', Settings.as_view()),
    path('api/auth/', include('rest_framework.urls', namespace='rest_framework')),
    path('admin/', admin.site.urls),

    path('api/set-csrf/', set_csrf_token, name='Set-CSRF'),
    path('api/login/', login_view, name='Login'),
    path('api/logout/', logout_view, name='Logout'),

    path('api/openapi', get_schema_view(
            title="Tank",
            description="API using tank backend",
            version="0.0.1"
        ), name='openapi-schema'),
]

