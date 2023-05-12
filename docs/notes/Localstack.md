# Running against [LocalStack](https://github.com/localstack)

```shell
npm install -g aws-cdk-local aws-cdk
pip install awscli-local
```

```
docker run --rm -it -p 4566:4566 -p 4510-4559:4510-4559 -e SERVICES=s3,events,lambda,stepfunctions -e EAGER_SERVICE_LOADING=1 -v /var/run/docker.sock:/var/run/docker.sock localstack/localstack
```

```shell
cdklocal bootstrap aws://000000000000/us-west-2
```
