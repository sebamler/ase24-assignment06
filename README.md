# TaskBoard (ASE WS 2024/25)

## Project Description

The core idea is to develop a simplified version of task board, similar to GitHub Projects boards, Trello, or Jira.
Users create tasks, move tasks between columns, assign tasks to team members, and delete tasks.

The main domain events are:

* `TaskCreated`
* `TaskAssigned`
* `TaskUpdated` (e.g., changed title, description, status, etc.)
* `TaskDeleted`

Potential extensions:
* `TaskCommented` (e.g., add a comment to a task)

Potential projections:

* A board view of tasks by status/column (e.g., `TODO`, `DOING`, `DONE`).
* A board view per user (a user's personal task list by assignment).
* An audit log per task (who created it, who assigned it, how many times it moved, who deleted it).

## Architecture

The backend is implemented as a Spring Boot application with a PostgreSQL database.
DTOs are used to map the domain model to the REST API.
Bean validation is used to validate the DTOs before processing the requests.
The domain objects are only validated statically using the `@NotNull` annotation (from `org.springframework.lang`).
The application is containerized using Docker and requires a Docker daemon for local testing.

In this iteration, the application will be re-engineered to use CQRS and event sourcing.
Due to the abstraction that the business/domain core provides, this can be done almost without changing the core.
The controllers/the REST API also won't be affected.

After that re-engineering, the containerized application will be deployed to a cloud environment.
As part of a future assignment, we will also add a frontend.

## Spring Boot Web Application

### Build and start application with dev profile

**Note:** In the `dev` profile, the repositories are cleared before startup and the initial data is loaded (see [`LoadInitialData.java`](https://github.com/se-ubt/ase24-taskboard/blob/main/application/src/main/java/de/unibayreuth/se/taskboard/LoadInitialData.java)).

Build application:
```shell
mvn clean install
```

Start Postgres docker container:
```shell
docker run -d -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine
```

Start application (data source configured via [`application.yaml`](application/src/main/resources/application.yaml)):
```shell
cd application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker

#### Building an app image from the Dockerfile

```shell
docker build -t taskboard:latest .
```

#### Create and run a Docker container based on the image (start DB container first)

```shell
docker network create container-net
docker run -d --name db --net container-net -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 postgres:16-alpine
docker run --net container-net -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/postgres -e SPRING_DATASOURCE_USERNAME=postgres -e SPRING_DATASOURCE_PASSWORD=postgres -it --rm taskboard:latest
```

`-it`  runs a container in interactive mode with a pseudo-TTY (terminal).
`--rm` automatically removes the container (and its associated resources) if it exists already.<br/>


#### Using Docker compose to run the app together with the DB container

Build container image:

```shell
docker compose build
```

Create and start containers:

```shell
docker compose up
```

Stop and remove containers and networks:

```shell
docker compose down
```

### Deployment with Dokku

#### Push the Docker image with the application to Docker Hub

Login to Docker Hub:
```shell
docker login -u sbaltes
```

Build and push the image:
```shell
mvn clean install
docker build --platform linux/amd64 -t sbaltes/taskboard-x86:latest .
docker push sbaltes/taskboard-x86:latest
```

You can then check the availability of the image on [Docker Hub](https://hub.docker.com/repository/docker/sbaltes/taskboard-x86).

#### Provision a VM instance in the cloud

Provision a VM instance in the cloud (e.g., AWS, Azure, GCP, etc.) and install Dokku on it:

* We are using a [Google Cloud Compute Engine VM instance](https://cloud.google.com/compute/docs/instances) of type `e2-medium` with Ubuntu 24.04 LTS x86-64 in region `europe-west3` (Frankfurt) and 20 GB persistent storage
* We also configured a [static external IP address](https://cloud.google.com/compute/docs/ip-addresses/configure-static-external-ip-address) and pointed two subdomains of a domain we own (`baltes.cloud`) to that IP address using DNS A records: `dokku.baltes.cloud` for accessing the VM and `taskoard.baltes.cloud` for later deploying the application.
* We configured the VM hostname accordingly (`dokku.baltes.cloud`) and set the firewall to allow HTTP and HTTPS traffic (the former is required for LetsEncrypt to work).
* We also need to [add our public SSH key](https://cloud.google.com/compute/docs/connect/add-ssh-keys) to the Google Cloud account so that it is automatically added to the VM instance (manually adding it to the `~/.ssh/authorized_keys` won't survive a VM restart). We need to add that key both for the regular VM user and username `dokku` (which is created by the Dokku installation script).

We can then SSH into the VM instance using the external IP address or the subdomain.
Google Cloud also provides a web-based SSH terminal.

#### Install Dokku on the VM instance

First, we update and upgrade the installed packages:

```shell
sudo apt update
sudo apt upgrade
```

Then, we install Dokku according to the [official documentation](https://dokku.com/docs/getting-started/installation/).
Dokku always expects that the complete domain is used for Dokku and the app name is automatically available under a subdomain.
While we want to access the VM via `dokku.baltes.cloud`, we want to access the app via `taskboard.baltes.cloud`.
Therefore, we need to set the global domain to `baltes.cloud`.

```shell
# installs Dokku via apt-get
wget -NP . https://dokku.com/install/v0.35.14/bootstrap.sh
sudo DOKKU_TAG=v0.35.14 bash bootstrap.sh
```

Depending on the cloud provider, you might have to restart the VM to import the SSH keys for the newly created `dokku` user.
Also, Google Cloud adds lines with `# Added by Google` to the `~/.ssh/authorized_keys` file, which might cause issues with Dokku.
You can temporarily remove these lines and then add your key(s).

```shell
# usually your key is already available under the current user's `~/.ssh/authorized_keys` file
cat ~/.ssh/authorized_keys | sudo dokku ssh-keys:add admin

# you can use any domain you already have access to
# this domain should have an A record or CNAME pointing at your server's IP
dokku domains:set-global baltes.cloud
```

#### Deploy the application to Dokku

We start by creating a new Dokku app for our [deployment](https://dokku.com/docs/deployment/application-deployment/#create-the-app):

```shell
dokku apps:create taskboard
```

Then we create a [backing service](https://dokku.com/docs/deployment/application-deployment/#create-the-backing-services) for the PostgreSQL database and link it to our app:

```shell
# install the postgres plugin
sudo dokku plugin:install https://github.com/dokku/dokku-postgres.git

# create a postgres (version 16) service
dokku postgres:create taskboard-db -I 16

# each official datastore offers a `link` method to link a service to any application
dokku postgres:link taskboard-db taskboard
# ...
#  !     App image (dokku/taskboard:latest) not found
```

We can ignore the warning about the missing app image for now, because will configure the correct image later.
Next, we need to set the [environment variables](https://dokku.com/docs/configuration/environment-variables/) with the database connection details so that Sprint Boot can connect to the database:

```shell
dokku config:show taskboard
# =====> taskboard env vars
# DATABASE_URL:  postgres://postgres:32801db59966d0783730c434ed4e162d@dokku-postgres-taskboard-db:5432/taskboard_db
# Format: protocol://username:password@host:port/database

dokku config:set --no-restart taskboard SPRING_DATASOURCE_URL=jdbc:postgresql://dokku-postgres-taskboard-db:5432/taskboard_db
dokku config:set --no-restart taskboard SPRING_DATASOURCE_USERNAME=postgres
dokku config:set --no-restart taskboard SPRING_DATASOURCE_PASSWORD=32801db59966d0783730c434ed4e162d
```

We will also set an environment variable to activate the `prod` profile:

```shell
dokku config:set --no-restart taskboard SPRING_PROFILES_ACTIVE=prod
```

Next step is to verify that the domain for the app is set correctly:

```shell
dokku domains:report taskboard
# =====> taskboard domains information
#       Domains app enabled:           true
#       Domains app vhosts:            taskboard.dokku.baltes.cloud
#       Domains global enabled:        true
#       Domains global vhosts:         dokku.baltes.cloud
```

If the domains are not correctly configured, we can set them using the following command:

```shell
dokku domains:set-global baltes.cloud
dokku domains:set taskboard taskboard.baltes.cloud
```

Now we can [deploy the application](https://dokku.com/docs/deployment/methods/image/) using the previously uploaded Docker image:

```shell
dokku registry:set taskboard server docker.io
dokku registry:set taskboard image-repo sbaltes/taskboard-x86
echo "YOUR_PASSWORD_OR_ACCESS_TOKEN" | dokku registry:login --password-stdin docker.io sbaltes
dokku git:from-image taskboard sbaltes/taskboard-x86:latest
# ...
# =====> Application deployed:
#        http://taskboard.baltes.cloud:8080
# now we log out of the Docker registry again because we are not using a credentials store locally, hence the password is stored base64-encoded in the Docker config file (/home/dokku/.docker/config.json).
sudo -u dokku docker logout
```

We can verify that the application successfully started by [checking the logs](https://dokku.com/docs/deployment/logs/):

```shell
dokku logs taskboard
# ...
# 2025-01-16T19:07:29.173200420Z app[web.1]: 2025-01-16T19:07:29.172Z  INFO 7 --- [taskboard] [main] d.unibayreuth.se.taskboard.Application : Started Application in 21.652 seconds (process running for 24.323)
```

We can now try to call the `GET` endpoint `/api/tasks` from the VM:

```shell
curl http://localhost:8080/api/tasks
# []
```

Great, this works! The response is an empty list.
The next step is to call the same endpoint from your local machine:
    
```shell    
curl http://taskboard.baltes.cloud:8080/api/tasks
# curl: (28) Failed to connect to taskboard.baltes.cloud port 8080 after 75028 ms: Couldn't connect to server
```

This doesn't work because the port 8080 is not open in the firewall.
We need to use [Dokku's port mancurl http://taskboard.baltes.cloud:8080/api/tasksagement](https://dokku.com/docs/networking/port-management/):

```shell
dokku ports:list taskboard
#  !     No port mappings configured for app
```

Since we want to use HTTPS, we need to set up SSL first.
This can be done using [Let's Encrypt](https://github.com/dokku/dokku-letsencrypt):

```shell
# install the letsencrypt plugin
sudo dokku plugin:install https://github.com/dokku/dokku-letsencrypt.git
```

Now we can obtain a Let's encrypt TLS certificate for our app.
This command can also be used to renew an existing certificate.
It is important that port 80 is available for the certificate generation to work.

```shell
dokku letsencrypt:set taskboard email sebastian.baltes@uni-bayreuth.de
dokku ports:set taskboard http:80:80 # required for Let's Encrypt certificate generation to work
dokku letsencrypt:enable taskboard
 # enable automatic certificate renewal
dokku letsencrypt:cron-job --add
```

Now let's set the HTTPS port mapping and [redeploy](https://dokku.com/docs/deployment/application-deployment/#redeploying-or-restarting) our app and try again via https:

```shell
dokku ports:set taskboard https:443:8080
dokku ports:report taskboard
# =====> taskboard ports information
#        Ports map:                     https:443:8080
#        Ports map detected:            https:8080:8080
dokku ps:rebuild taskboard
# =====> Application deployed:
#        https://taskboard.baltes.cloud
```

Now we can try to call the endpoint again from our local machine:

```shell 
curl https://taskboard.baltes.cloud/api/tasks
# []
```

And it works!

#### Health checks

According to the [documentation](https://dokku.com/docs/deployment/zero-downtime-deploys/), "by default, Dokku will wait 10 seconds after starting each container before assuming it is up and proceeding with the deploy."
We implemented an additional health check using Spring Boot Actuator (see [app.json](app.json)).
This file is copied into the Docker image and detected by Dokku upon deployment.

### REST requests (tasks)

#### Get tasks

All tasks:
```shell
curl http://localhost:8080/api/tasks
```
Task by ID:
```shell
curl http://localhost:8080/api/tasks/4221a32e-3e2a-4bc8-9ae7-8249ea68dfd9 # add valid task id here
```

Tasks by status:
```shell
curl http://localhost:8080/api/tasks/status/TODO # add valid task status here
```

Tasks by assignee:
```shell
curl http://localhost:8080/api/tasks/assignee/4749f527-e240-4b9c-bc6c-5e1b744d553e # add valid user id here
```

#### Create task

```shell
curl --header "Content-Type: application/json" --request POST --data '{"title":"Task title","description":"Task description"}' https://taskboard.baltes.cloud/api/tasks
```

#### Update task

Update title and description:
```shell
curl --header "Content-Type: application/json" --request PUT --data '{"id":"238ce5b2-9d85-43f7-a90e-172ae3ab0d28","title":"New title","description":"New description"}' http://localhost:8080/api/tasks/238ce5b2-9d85-43f7-a90e-172ae3ab0d28 # add valid task id
```

Update status:
```shell
curl --header "Content-Type: application/json" --request PUT --data '{"id":"238ce5b2-9d85-43f7-a90e-172ae3ab0d28","title":"New title","description":"New description", "status":"DOING"}' http://localhost:8080/api/tasks/238ce5b2-9d85-43f7-a90e-172ae3ab0d28 # add valid task id
```

Assign user:
```shell
curl --header "Content-Type: application/json" --request PUT --data '{"id":"238ce5b2-9d85-43f7-a90e-172ae3ab0d28","title":"New title","description":"New description","status":"DOING", "assignee": {"id":"4749f527-e240-4b9c-bc6c-5e1b744d553e", "name":"Charlie"}}' http://localhost:8080/api/tasks/238ce5b2-9d85-43f7-a90e-172ae3ab0d28 # add valid task id and user id
```

### REST requests (users)

#### Get users

All users:

```shell
curl http://localhost:8080/api/users
```

User by ID:

```shell
curl http://localhost:8080/api/users/6900289d-6905-4898-b05a-3d96ededdd73 # add valid user id
```

### Create user

```shell
 curl --header "Content-Type: application/json" --request POST --data '{"name": "Denise"}' http://localhost:8080/api/users  
```
