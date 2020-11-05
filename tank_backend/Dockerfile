FROM python:alpine

WORKDIR /app

# set environment variables
ENV PYTHONUNBUFFERED 1

# install psycopg2 dependencies (o use postgre database)
RUN apk update \
    && apk add postgresql-dev gcc python3-dev musl-dev

# install dependencies
COPY requirements.txt /app/
RUN pip install -r requirements.txt

# copy entrypoint.sh
COPY ./entrypoint.sh .

# copy project
COPY . .

# run entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]