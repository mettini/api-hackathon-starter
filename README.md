# Api Hackathon Starter

It is a hackathon starter made in Scala (Play Framework). The idea is to have a base project in which to start a hackathon or use it as a boilerplate to start new a project.

It covers the base structure of the project, users CRUD and API security scheme.

It aims to be an API application. In the case that the nature of the project is only Web-based, take a look to [Web Hackathon Starter](https://github.com/mettini/web-hackathon-starter).

Table of Contents
-----------------

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Obtaining API Keys](#obtaining-api-keys)
- [Contributing](#contributing)
- [License](#license)

Features
--------

- Authentication using email and password
- Security scheme for logged or unlogged api services
- Email send through Sendgrid (dev mode logging emails in output log)
- Functional & integrational tests for auth and user flows
- **Account management**
    - Edit profile
    - Email verification
    - Change password
    - Delete user

Prerequisites
-------------
- [Mysql](http://www.mysql.com)
- [Java](https://www.java.com/es/download/)
- [SBT](https://www.scala-sbt.org/)

Getting Started
---------------

Clone the repo and follow these steps to leave the app running:

### Database init

Create a database with name *hackathonStarter*:
```sql
DROP DATABASE IF EXISTS hackathonStarter;
CREATE DATABASE hackathonStarter DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE hackathonStarter;
```

Create table structure and insert base data running `conf/evolutions/default/1.sql` and `conf/evolutions/default/2.sql` (only the *!Ups* sections in both files).

### Server startup

Open a console, and run `sbt` command. Once the sbt is running execute `run`.
When the startup has finished (first time it take a while to download all dependencies), you can call any api service on `http://localhost:9000` endpoint.

To run the tests, once inside `sbt` console execute `test`.

Obtaining API Keys
------------------

By default the email sent is turn off (all emails will be print in the output log). To activate it follow the next steps:

- Create an account at [Sendgrid](https://sendgrid.com/)
- Obtain an api key and change the default value at `conf/application.conf` in `app.sendgrid.apikey` param. You may want to change the `app.sendgrid.from` param too.
- Turn on email send by changing to `true` the `email.enabled` param.

## Contributing

Thank you for considering contributing to Api Hackathon Starter.

## License

The MIT License (MIT). Please see [License File](LICENSE) for more information.