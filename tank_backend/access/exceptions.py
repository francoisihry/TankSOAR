from rest_framework import exceptions, status, views


# This exception method is necessary to raise 401 exception instead of 403, to allow the
# frontend to track when user session is over
# More info here: https://github.com/encode/django-rest-framework/issues/5968
def custom_exception_handler(exc, context):
    response = views.exception_handler(exc, context)
    if isinstance(exc, (exceptions.AuthenticationFailed, exceptions.NotAuthenticated)):
        response.status_code = status.HTTP_401_UNAUTHORIZED
    return response
