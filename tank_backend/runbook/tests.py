from django.core.exceptions import ValidationError
from django.db.utils import IntegrityError
from django.test import TestCase
from rest_framework import status
from django.contrib.auth import get_user_model

import json

from access.models import Role
from .models import Runbook
from .serializers import RunbookSerializer
from access.tests import get_login_token


class RunbookTestCase(TestCase):
    def test_runbook_instance(self):
        rb = Runbook(name='test', language='python', content="print('hello kaamelott')")
        rb.save()
        rb.full_clean()
        rb = Runbook.objects.get(name='test')
        self.assertEqual(rb.language, 'python')
        self.assertEqual(rb.name, 'test')
        self.assertEqual(rb.content, "print('hello kaamelott')")

    def test_incorrect_language(self):
        rb = Runbook.objects.create(name='test', language='nothing', content="print('hello kaamelott')")
        with self.assertRaises(ValidationError):
            rb.full_clean()

    def test_runbook_serializer(self):
        rb = Runbook.objects.create(name='test', language='js', content="print('hello kaamelott')")
        serialized_rb = RunbookSerializer(rb)
        self.assertEqual(serialized_rb.data['name'], 'test')
        self.assertEqual(serialized_rb.data['language'], 'js')
        self.assertEqual(serialized_rb.data['content'], "print('hello kaamelott')")

    def test_runbook_deserialization(self):
        data = {"name": "my_runbook", "language": "sh", 'content': 'ls -la|grep hello'}
        deserialized = RunbookSerializer(data=data)
        is_valid = deserialized.is_valid()
        self.assertTrue(is_valid)
        validated_data = deserialized.validated_data
        self.assertEqual(dict(validated_data), {'name': 'my_runbook',
                                                'language': 'sh',
                                                'content': 'ls -la|grep hello'
                                                })

    def test_can_create_only_one_name(self):
        Runbook.objects.create(name='test', language='js', content="print('hello kaamelott')")
        with self.assertRaises(IntegrityError):
            Runbook.objects.create(name='test', language='python', content="print('hello kaamelott')")

    def test_cannot_create_runbook_with_space(self):
        # create user karadok
        user = get_user_model().objects.create_user(username='karadok', password='tankSOAR2!!!', roles=[Role.DEVELOPER])
        token = get_login_token(self, 'karadok', 'tankSOAR2!!!')

        runbook_data = {
            'name': 'my runbook',
            'language': 'python',
            'content': 'print("I am a great runbook!")'
        }

        # post the script
        resp = self.client.post('/api/runbooks/',
                                HTTP_AUTHORIZATION='Bearer {}'.format(token),
                                content_type="application/json",
                                data=json.dumps(runbook_data))
        self.assertEqual(resp.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertEqual(resp.content, b'{"name":["Enter a valid runbook name. This value may contain only letters,numbers, and @/./-/_ characters."]}')

    def test_runbook_update_name_replace(self):
        # create a runbook
        rb = Runbook.objects.create(name='test', language='python', content="print('hello kaamelott')")
        rb.save()

        rb.name = 'new_name'
        rb.save()

        # check contains the runbook with the new name
        self.assertEqual(Runbook.objects.all().filter(name='new_name').count(), 1)

        # check the older name does not exist anymore
        self.assertEqual(Runbook.objects.all().filter(name='test').count(), 0)


class UrlsTestCase(TestCase):
    def setUp(self):
        # create user karadok
        User = get_user_model()
        self.karadok = User.objects.create_user(username='karadok', password='tankSOAR2!!!')
        self.csrftoken = get_login_token(self, 'karadok', 'tankSOAR2!!!')

    def test_get_runbook(self):
        # create a runbook
        rb = Runbook.objects.create(name='test', language='python', content="print('hello kaamelott')")

        # get the runbook with its id
        resp = self.client.get('/api/runbooks/test/', Cookie='csrftoken={}'.format(self.csrftoken))
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertEqual(resp.data['name'], 'test')
        self.assertEqual(resp.data['language'], 'python')
        self.assertEqual(resp.data['content'], "print('hello kaamelott')")

    def test_post_runbook_being_a_developper(self):
        # set the dev role to karadok
        self.karadok.roles.set(list(Role.objects.filter(id=Role.DEVELOPER)))
        self.karadok.save()

        runbook_data = {
            'name': 'my_powerful_runbook',
            'language': 'python',
            'content': 'print("I am a great runbook!")'
        }

        # post the script
        resp = self.client.post('/api/runbooks/',
                                Cookie='csrftoken={}'.format(self.csrftoken),
                                content_type="application/json",
                                data=json.dumps(runbook_data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])

        rb = Runbook.objects.get(name='my_powerful_runbook')
        self.assertEqual(rb.name, 'my_powerful_runbook')
        self.assertEqual(rb.language, 'python')
        self.assertEqual(rb.content, 'print("I am a great runbook!")')

    def test_post_runbook_with_empty_content(self):
        # set the dev role to karadok
        self.karadok.roles.set(list(Role.objects.filter(id=Role.DEVELOPER)))
        self.karadok.save()

        runbook_data = {
            'name': 'my_powerful_runbook',
            'language': 'python',
            'content': ''
        }

        # post the script
        resp = self.client.post('/api/runbooks/',
                                Cookie='csrftoken={}'.format(self.csrftoken),
                                content_type="application/json",
                                data=json.dumps(runbook_data))
        self.assertIn(resp.status_code, [status.HTTP_200_OK, status.HTTP_201_CREATED])


    def test_post_runbook_with_incorrect_language(self):
        # set the dev role to karadok
        self.karadok.roles.set(list(Role.objects.filter(id=Role.DEVELOPER)))
        self.karadok.save()

        runbook_data = {
            'name': 'my_powerful_runbook',
            'language': 'incorrect',
            'content': 'print("I am a great runbook!")'
        }
        # post the script
        resp = self.client.post('/api/runbooks/',
                                Cookie='csrftoken={}'.format(self.csrftoken),
                                content_type="application/json",
                                data=json.dumps(runbook_data))
        self.assertEqual(resp.status_code, status.HTTP_400_BAD_REQUEST)

    def test_get_runbook_list(self):
        runbook_list = [('hello', 'python', 'print("hello")'),
                        ('c_est_pas_faux', 'js', 'console.log("i am perceval")'),
                        ('get_processes', 'shell', 'ps aux')
                        ]
        for rb in runbook_list:
            Runbook.objects.create(name=rb[0], language=rb[1], content=rb[2])

        # get all the runbooks
        resp = self.client.get('/api/runbooks/', Cookie='csrftoken={}'.format(self.csrftoken))
        for rb in runbook_list:
            self.assertIn(rb, [(r['name'], r['language'], r['content']) for r in resp.json()])

    def test_runbook_update_being_a_developper(self):
        # create a runbook
        rb = Runbook.objects.create(name='test', language='python', content="print('hello kaamelott')")

        # set dev role to karadok
        self.karadok.roles.set(list(Role.objects.filter(id=Role.DEVELOPER)))
        self.karadok.save()

        # get the runbook with its id
        resp = self.client.get('/api/runbooks/test/', Cookie='csrftoken={}'.format(self.csrftoken))
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertEqual(resp.data['content'], "print('hello kaamelott')")
        self.assertEqual(resp.data['created_at'][:-4], resp.data['updated_at'][:-4])

        # update the runbook
        resp = self.client.patch('/api/runbooks/test/',
                                 Cookie='csrftoken={}'.format(self.csrftoken),
                                 content_type="application/json",
                                 data=json.dumps({'content': 'content update'}))
        self.assertNotEqual(resp.data['created_at'][:-3], resp.data['updated_at'][:-3])

    def test_runbook_update_name_replace(self):
        # create a runbook
        rb = Runbook.objects.create(name='test', language='python', content="print('hello kaamelott')")

        # set dev role to karadok
        self.karadok.roles.set(list(Role.objects.filter(id=Role.DEVELOPER)))
        self.karadok.save()

        # get the runbook with its id
        resp = self.client.get('/api/runbooks/test/', Cookie='csrftoken={}'.format(self.csrftoken))
        self.assertEqual(resp.status_code, status.HTTP_200_OK)
        self.assertEqual(resp.data['content'], "print('hello kaamelott')")
        self.assertEqual(resp.data['created_at'][:-4], resp.data['updated_at'][:-4])

        # update the runbook
        resp = self.client.patch('/api/runbooks/test/',
                                 Cookie='csrftoken={}'.format(self.csrftoken),
                                 content_type="application/json",
                                 data=json.dumps({'name': 'new_name'}))

        # check contains the runbook with the new name
        self.assertEqual(Runbook.objects.all().filter(name='new_name').count(), 1)

        # check the older name does not exist anymore
        self.assertEqual(Runbook.objects.all().filter(name='test').count(), 0)


class PermissionTestCase(TestCase):
    def setUp(self):
        # create user karadok
        User = get_user_model()
        self.karadok = User.objects.create_user(username='karadok', password='tankSOAR2!!!')
        self.csrftoken = get_login_token(self, 'karadok', 'tankSOAR2!!!')

    def test_cannot_post_runbook(self):
        runbook_data = {
            'name': 'test',
            'language': 'shell',
            'content': 'print("i am a bad attackant")'
        }

        # post the script
        resp = self.client.post('/api/runbooks/',
                                Cookie='csrftoken={}'.format(self.csrftoken),
                                content_type="application/json",
                                data=json.dumps(runbook_data))
        self.assertEqual(resp.status_code, status.HTTP_403_FORBIDDEN)
