from django.test import TestCase
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APITestCase
from django.contrib.auth import get_user_model

import jwt
import json

from .models import TankUser, Role, Settings
from .serializers import RoleSerializer, UserSerializer, SettingsSerializer


def get_login_token(testcase_instance, username, password):
    # obtain csrf token for user
    url_csrf_token = reverse('Set-CSRF')
    resp = testcase_instance.client.get(url_csrf_token)
    csrftoken = resp.cookies['csrftoken'].value

    # login user with csrf session
    url_login = reverse('Login')
    testcase_instance.client.post(url_login, {'username': username, 'password': password},
                            Cookie='csrftoken={}'.format(csrftoken), content_type="application/json")
    return csrftoken


class TankUserTestCase(TestCase):
    def setUp(self):
        # create admin user
        UserModel = get_user_model()
        self.new_admin = UserModel.objects.create_superuser(username='new_admin', password='NimdA!!')
        self.admin_csrftoken = get_login_token(self, 'new_admin', 'NimdA!!')

    def test_user_instance(self):
        UserModel = get_user_model()
        self.assertEquals(UserModel, TankUser)
        new_user = UserModel.objects.create_user(username='karadok', password='tankSOAR2!')
        self.assertIsInstance(new_user, TankUser)

    def test_user_instance_with_email(self):
        UserModel = get_user_model()
        self.assertEquals(UserModel, TankUser)
        new_user = UserModel.objects.create_user(username='karadok', password='tankSOAR2!',
                                                 email='karadok@kaamelott.br')
        self.assertEqual(new_user.email, 'karadok@kaamelott.br')

    def test_user_serializer(self):
        new_user = TankUser.objects.create_user(username='gauvin', password='n1vuag')
        serialized_new_user = UserSerializer(new_user)
        self.assertEqual(serialized_new_user.data['username'], new_user.username)

    def test_user_deserialization(self):
        data = {"username": "gauvin", "password": "n1vuag"}
        deserialized = UserSerializer(data=data)
        is_valid = deserialized.is_valid()
        self.assertTrue(is_valid)
        validated_data = deserialized.validated_data
        self.assertEqual(dict(validated_data), {'username': 'gauvin',
                                                'password': 'n1vuag',
                                                'first_name': None,
                                                'last_name': None,
                                                'roles': None,
                                                'email': None,
                                                'last_login': None})

    def test_user_with_roles_serializer(self):
        new_user = TankUser.objects.create_user(username='gauvin', password='n1vuag',
                                                roles=[Role.ADMIN, Role.DEVELOPER])
        serialized_new_user = UserSerializer(new_user)
        self.assertEqual(serialized_new_user.data['username'], new_user.username)
        self.assertEqual(len(serialized_new_user.data['roles']), 2)
        self.assertIn({'id': Role.ADMIN}, serialized_new_user.data['roles'])
        self.assertIn({'id': Role.DEVELOPER}, serialized_new_user.data['roles'])

    def test_user_with_email_serializer(self):
        new_user = TankUser.objects.create_user(username='gauvin', password='n1vuag',
                                                email='gauvin@kaamelott.fr')
        serialized_new_user = UserSerializer(new_user)
        self.assertEqual(serialized_new_user.data['email'], 'gauvin@kaamelott.fr')

    def test_user_with_roles_serializer_from_json(self):
        data = {'username': 'gauvin',
                'password': 'n1vuag',
                'roles': [{'id': Role.DEVELOPER},
                          {'id': Role.ANALYST},
                          ]
                }
        new_user = UserSerializer(data=data)
        self.assertTrue(new_user.is_valid())
        new_user_object = new_user.save()
        self.assertIn(Role(Role.DEVELOPER), list(new_user_object.roles.all()))
        self.assertIn(Role(Role.ANALYST), list(new_user_object.roles.all()))

    def test_user_instance_with_roles_through_api(self):
        # create gauvin user with an API request*
        data = {'username': 'gauvin',
                'password': 'n1vuag',
                'roles': [{'id': Role.DEVELOPER},
                          {'id': Role.ANALYST},
                          ]
                }
        resp = self.client.post('/api/users/',
                                Cookie='csrftoken={}'.format(self.admin_csrftoken),
                                content_type="application/json",
                                data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        self.assertEqual(len(TankUser.objects.all().filter(username='gauvin')), 1)
        gauvin = TankUser.objects.get(username='gauvin')
        self.assertIn(Role(Role.DEVELOPER), list(gauvin.roles.all()))
        self.assertIn(Role(Role.ANALYST), list(gauvin.roles.all()))

    def test_user_instance_without_roles_through_api(self):
        # create gauvin user with an API request*
        data = {'username': 'gauvin',
                'password': 'n1vuag'
                }
        resp = self.client.post('/api/users/',
                                Cookie='csrftoken={}'.format(self.admin_csrftoken),
                                content_type="application/json",
                                data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        self.assertEqual(len(TankUser.objects.all().filter(username='gauvin')), 1)
        gauvin = TankUser.objects.get(username='gauvin')
        self.assertEqual(len(list(gauvin.roles.all())), 0)

    def test_user_instance_with_email_through_api(self):
        # create gauvin user with an API request*
        data = {'username': 'gauvin',
                'password': 'n1vuag',
                'email': 'gauvin@kaamelott.br'
                }
        resp = self.client.post('/api/users/',
                                Cookie='csrftoken={}'.format(self.admin_csrftoken),
                                content_type="application/json",
                                data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        gauvin = TankUser.objects.get(username='gauvin')
        self.assertEqual(gauvin.email, 'gauvin@kaamelott.br')

    def test_user_already_exists(self):
        # create karadok user with an API request*
        karadok = TankUser.objects.create_user(username="karadok", password="le_gras_cest_la_VIE!")
        karadok.save()

        # create karadok user for 2nd time (using the api)
        data = {'username': 'karadok',
                'password': 'le_gras_cest_la_VIE!'
                }
        resp = self.client.post('/api/users/',
                                Cookie='csrftoken={}'.format(self.admin_csrftoken),
                                content_type="application/json",
                                data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertEqual(resp.json(), {'username': ['A user with that username already exists.']})

    def test_get_all_users(self):
        # create users gauvin, yvain, and arthur
        for user in [('gauvin', 'nivuag'), ('yvain', 'niavy'), ('arthur', 'ruhtra')]:
            user = TankUser.objects.create_user(username=user[0], password=user[1])
            user.save()
        user_with_email = TankUser.objects.create_user(username='karadok', password='on_en_a_gros!',
                                                       email='karadok@kaamelott.br')
        user_with_email.save()

        # get the users
        resp = self.client.get('/api/users/',
                               Cookie='csrftoken={}'.format(self.admin_csrftoken))
        for user in ['gauvin', 'yvain', 'arthur', 'karadok']:
            self.assertIn(user, [r['username'] for r in resp.json()])

        karadok = [r for r in resp.json() if r['username'] == 'karadok'][0]
        self.assertEqual(karadok['email'], 'karadok@kaamelott.br')

    def test_get_specific_user_by_name(self):
        # create user perceval
        perceval = TankUser.objects.create_user(username="perceval", email='perceval@kaamelott.bretagne',
                                                password="on_en_a_GROS!")
        perceval.save()
        self.assertEqual(len(TankUser.objects.all().filter(id=perceval.id)), 1)

        # get the user by id

        resp = self.client.get('/api/users/perceval/',
                               Cookie='csrftoken={}'.format(self.admin_csrftoken))
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertEqual(resp.json()['username'], 'perceval')
        self.assertEqual(resp.json()['email'], 'perceval@kaamelott.bretagne')

    def test_delete_a_user_by_name(self):
        # create user perceval
        perceval = TankUser.objects.create_user(username="p3rc3v@l.deGalles", password="on_en_a_GROS!")
        perceval.save()
        self.assertEqual(len(TankUser.objects.all().filter(id=perceval.id)), 1)

        # delete the user
        resp = self.client.delete('/api/users/p3rc3v@l.deGalles/',
                                  Cookie='csrftoken={}'.format(self.admin_csrftoken))
        self.assertEqual(resp.status_code, status.HTTP_204_NO_CONTENT)
        self.assertEqual(len(TankUser.objects.all().filter(id=perceval.id)), 0)

    def test_update_user_name_of_a_simple_user_being_admin(self):
        # create user perceval
        perceval = TankUser.objects.create_user(username="perceval", password="on_en_a_GROS!")
        perceval.save()

        # update perceval username
        data = {'username': 'perceval_new_name'}
        resp = self.client.patch('/api/users/perceval/',
                                 Cookie='csrftoken={}'.format(self.admin_csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        self.assertEqual(TankUser.objects.all().filter(id=perceval.id)[0].username, 'perceval_new_name')

    def test_update_user_role_of_a_simple_user_being_admin(self):
        # create user perceval
        perceval = TankUser.objects.create_user(username="perceval", password="on_en_a_GROS!")
        perceval.save()

        # update perceval username
        data = {'roles': [{'id': Role.DEVELOPER},
                          {'id': Role.ANALYST},
                          ]}
        resp = self.client.patch('/api/users/perceval/',
                                 Cookie='csrftoken={}'.format(self.admin_csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        perceval = TankUser.objects.get(username='perceval')
        self.assertIn(Role(Role.DEVELOPER), list(perceval.roles.all()))
        self.assertIn(Role(Role.ANALYST), list(perceval.roles.all()))

    def test_last_login_field_update(self):
        # create user perceval
        perceval = TankUser.objects.create_user(username="perceval", password="on_en_a_GROS!")
        perceval.save()

        # login
        csrftoken = get_login_token(self, 'perceval', 'on_en_a_GROS!')
        resp = self.client.get('/api/me/', Cookie='csrftoken={}'.format(csrftoken), format='json')
        last_login = resp.json()['last_login']
        self.assertIsNotNone(last_login)

        # login again
        csrftoken = get_login_token(self, 'perceval', 'on_en_a_GROS!')
        resp = self.client.get('/api/me/', Cookie='csrftoken={}'.format(csrftoken), format='json')
        new_last_login = resp.json()['last_login']
        self.assertIsNotNone(new_last_login)
        self.assertNotEqual(last_login, new_last_login)

    def test_user_updates_its_timezone_setting(self):
        # create user perceval
        perceval = TankUser.objects.create_user(username="perceval", password="on_en_a_GROS!")
        perceval.save()
        self.assertEqual(perceval.settings.timezone, 'Europe/Paris')

        csrftoken = get_login_token(self, 'perceval', 'on_en_a_GROS!')
        # We set a new timezone
        data = {'timezone':'Europe/Belgrade'}
        resp = self.client.patch('/api/me/settings/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        perceval = TankUser.objects.get(username='perceval')
        # We check the timezone is changed
        self.assertEqual(perceval.settings.timezone, 'Europe/Belgrade')



class RoleTestCase(TestCase):
    def test_role_instance(self):
        # test admin role instance
        admin_role = Role(Role.ADMIN)
        admin_role.save()

        # test instance iterating over all roles
        for role_id, role in Role.ROLE_CHOICES:
            role_instance = Role(role_id)
            role_instance.save()
            self.assertEqual(str(role_instance), role)

    def test_role_serializer(self):
        admin_role = Role(Role.ADMIN)
        admin_role.save()
        serialized_admin_role = RoleSerializer(admin_role)
        self.assertEqual(serialized_admin_role.data['id'], admin_role.id)


class SettingsTestCase(TestCase):
    def test_settings_instance(self):
        # test settings instance
        settings = Settings()
        settings.save()
        # check default values
        self.assertEqual(settings.timezone, 'Europe/Paris')

        # test instance with some other timezones
        for tz in ['America/Los_Angeles', 'Europe/Belgrade', 'Asia/Almaty', 'Portugal']:
            settings = Settings(timezone=tz)
            settings.save()
            self.assertEqual(settings.timezone, tz)

    def test_settings_serializer(self):
        new_settings = Settings.objects.create(timezone='Europe/Belgrade')
        serialized_settings = SettingsSerializer(new_settings)
        self.assertEqual(serialized_settings.data['timezone'], new_settings.timezone)


class PermissionTestCase(TestCase):
    def setUp(self):
        # create user karadok
        User = get_user_model()
        self.karadok = User.objects.create_user(username='karadok', password='tankSOAR2!!!')

        # create user lancelot
        self.lancelot = User.objects.create_user(username='lancelot',
                                                 password='Ch3val13r_S0l1tair3',
                                                 email='lancelot@kaamelott.br')

    def test_unauthenticated_access(self):
        resp = self.client.get('/api/')
        self.assertEqual(resp.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_authenticated_access(self):
        csrftoken = get_login_token(self, 'karadok', 'tankSOAR2!!!')

        # access being authenticated
        resp = self.client.get('/api/', Cookie='csrftoken={}'.format(csrftoken), format='json')
        self.assertEqual(resp.status_code, status.HTTP_200_OK)

    def test_get_me(self):
        csrftoken = get_login_token(self, 'lancelot', 'Ch3val13r_S0l1tair3')

        # get me
        resp = self.client.get('/api/me/', Cookie='csrftoken={}'.format(csrftoken), format='json')
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertEqual(resp.data['username'], 'lancelot')
        self.assertEqual(resp.data['email'], 'lancelot@kaamelott.br')
        self.assertEqual(resp.data['settings']['timezone'], 'Europe/Paris')

    def test_add_tank_user_without_permission(self):
        # obtain csrf token for karadok
        url_csrf_token = reverse('Set-CSRF')
        resp = self.client.get(url_csrf_token)
        csrftoken = resp.cookies['csrftoken'].value

        # login karadok with csrf session
        url_login = reverse('Login')
        resp = self.client.post(url_login, {'username': 'karadok', 'password': 'tankSOAR2!!!'}, Cookie='csrftoken={}'.format(csrftoken), content_type="application/json")
        self.assertEqual(resp.status_code, status.HTTP_200_OK)

        # create gauvin user with an API request*
        data = {'username': 'gauvin',
                'password': 'n1vuag'
                }
        resp = self.client.post('/api/users/',
                                Cookie='csrftoken={}'.format(csrftoken),
                                content_type="application/json",
                                data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)

    def test_get_all_users_without_permission(self):
        # obtain csrf token for karadok
        url_csrf_token = reverse('Set-CSRF')
        resp = self.client.get(url_csrf_token)
        csrftoken = resp.cookies['csrftoken'].value
        # login karadok with csrf session
        url_login = reverse('Login')
        resp = self.client.post(url_login, {'username': 'karadok', 'password': 'tankSOAR2!!!'},
                                Cookie='csrftoken={}'.format(csrftoken), content_type="application/json")
        # get the users
        resp = self.client.get('/api/users/',
                               Cookie='csrftoken={}'.format(csrftoken), content_type="application/json")
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)

    def test_cannot_delete_tank_user_without_permission(self):
        # obtain csrf token for karadok
        url_csrf_token = reverse('Set-CSRF')
        resp = self.client.get(url_csrf_token)
        csrftoken = resp.cookies['csrftoken'].value
        # login karadok with csrf session
        url_login = reverse('Login')
        resp = self.client.post(url_login, {'username': 'karadok', 'password': 'tankSOAR2!!!'},
                                Cookie='csrftoken={}'.format(csrftoken), content_type="application/json")

        # create user perceval
        perceval = TankUser.objects.create_user(username="perceval", password="on_en_a_GROS!")
        perceval.save()

        # delete the user perceval
        resp = self.client.delete('/api/users/{}/'.format(perceval.id),
                                  Cookie='csrftoken={}'.format(csrftoken), format='json')
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)
        self.assertEqual(len(TankUser.objects.all().filter(id=perceval.id)), 1)

    def test_cannot_update_super_admin_being_admin(self):
        # create arthur user as admin
        arthur = TankUser.objects.create_superuser(username='arthur', password='tankSOAR!')
        arthur.save()
        self.assertEqual(arthur.is_admin, True)

        super_admin = TankUser.objects.get(username='admin')
        self.assertEqual(super_admin.is_super_admin, True)

        # try to update super admin username
        data = {'username': 'admin_new_name'}
        csrftoken = get_login_token(self, 'arthur', 'tankSOAR!')
        resp = self.client.patch('/api/users/admin/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)
        self.assertIsNotNone(TankUser.objects.get(username='admin'))

        # try to update super admin roles
        data = {'roles': [{'id': Role.DEVELOPER},
                          {'id': Role.ANALYST},
                          ]}
        resp = self.client.patch('/api/users/admin/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)

    def test_super_admin_cannot_update_its_username_and_roles(self):
        super_admin = TankUser.objects.get(username='admin')
        self.assertEqual(super_admin.is_super_admin, True)

        # super admin try to update its username
        data = {'username': 'admin_new_name'}
        csrftoken = get_login_token(self, 'admin', 'TankAdmin!')
        resp = self.client.patch('/api/users/admin/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)
        self.assertIsNotNone(TankUser.objects.get(username='admin'))

        # try to update super admin roles
        data = {'roles': [{'id': Role.DEVELOPER},
                          {'id': Role.ANALYST},
                          ]}
        resp = self.client.patch('/api/users/admin/'.format(super_admin.id),
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)

    def test_cannot_update_another_admin_being_admin(self):
        # create two admin users : arthur and perceval
        arthur = TankUser.objects.create_superuser(username='arthur', password='tankSOAR!')
        arthur.save()
        self.assertEqual(arthur.is_admin, True)
        perceval = TankUser.objects.create_superuser(username='perceval', password='tankSOAR!')
        perceval.save()
        self.assertEqual(perceval.is_admin, True)

        # arthur try to update perceval username
        data = {'username': 'perceval_new_name'}
        csrftoken = get_login_token(self, 'arthur', 'tankSOAR!')
        resp = self.client.patch('/api/users/perceval/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)
        self.assertIsNotNone(TankUser.objects.get(username='perceval'))

        # arthur try to update perceval roles
        data = {'roles': [{'id': Role.DEVELOPER},
                          {'id': Role.ANALYST},
                          ]}
        resp = self.client.patch('/api/users/perceval/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)

    def test_cannot_delete_another_admin_being_admin(self):
        # create two admin users : arthur and perceval
        arthur = TankUser.objects.create_superuser(username='arthur', password='tankSOAR!')
        arthur.save()
        self.assertEqual(arthur.is_admin, True)
        perceval = TankUser.objects.create_superuser(username='perceval', password='tankSOAR!')
        perceval.save()
        self.assertEqual(perceval.is_admin, True)

        # arthur try to delete perceval
        csrftoken = get_login_token(self, 'arthur', 'tankSOAR!')
        resp = self.client.delete('/api/users/perceval/',
                                  Cookie='csrftoken={}'.format(csrftoken))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)
        self.assertIsNotNone(TankUser.objects.get(username='perceval'))

    def test_can_update_another_admin_being_super_admin(self):
        # create arthur user as admin
        arthur = TankUser.objects.create_superuser(username='arthur', password='tankSOAR!')
        arthur.save()
        self.assertEqual(arthur.is_admin, True)

        super_admin = TankUser.objects.get(username='admin')
        self.assertEqual(super_admin.is_super_admin, True)

        # super admin updates arthur username
        data = {'username': 'arthur_new_name'}
        csrftoken = get_login_token(self, 'admin', 'TankAdmin!')
        resp = self.client.patch('/api/users/arthur/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        self.assertEquals(len(TankUser.objects.all().filter(username='arthur')), 0)
        self.assertIsNotNone(TankUser.objects.get(username='arthur_new_name'))

        # super admin updates arthur_new_name roles
        data = {'roles': [{'id': Role.DEVELOPER},
                          {'id': Role.ANALYST},
                          ]}
        resp = self.client.patch('/api/users/arthur_new_name/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])
        arthur = TankUser.objects.get(username='arthur_new_name')
        self.assertIn(Role(Role.DEVELOPER), list(arthur.roles.all()))
        self.assertIn(Role(Role.ANALYST), list(arthur.roles.all()))
        self.assertNotIn(Role.ADMIN, list(arthur.roles.all()))

    def test_super_admin_cannot_delete_himself(self):
        super_admin = TankUser.objects.get(username='admin')
        self.assertEqual(super_admin.is_super_admin, True)

        # super admin try to delete its account
        csrftoken = get_login_token(self, 'admin', 'TankAdmin!')
        resp = self.client.delete('/api/users/admin/',
                                  Cookie='csrftoken={}'.format(csrftoken))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)
        self.assertIsNotNone(TankUser.objects.get(username='admin'))

    def test_user_cannot_update_its_username(self):
        # create user perceval
        perceval = TankUser.objects.create_user(username="perceval", password="on_en_a_GROS!")
        perceval.save()

        self.assertEqual(perceval.settings.timezone, 'Europe/Paris')

        csrftoken = get_login_token(self, 'perceval', 'on_en_a_GROS!')
        data = {'username': 'perceval_new_name'}
        resp = self.client.patch('/api/users/me/',
                                 Cookie='csrftoken={}'.format(csrftoken),
                                 content_type="application/json",
                                 data=json.dumps(data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)