# Redis OM Spring Demo - Vector Similarity Search

This demo showcases the vector search similarity (VSS) capability within Redis Stack and Redis Enterprise.
Through the RediSearch module, vector types and indexes can be added to Redis. This turns Redis into
a highly performant vector database which can be used for all types of applications.

The following Redis Stack capabilities are available in this demo:
- **Vector Similarity Search**
    - by image
    - by text
- **Hybrid Queries**
    - Apply tags as pre-filter for vector search

## Application

This app was built as a Single Page Application (SPA) with the following components:

- **[Redis Stack](https://redis.io/docs/stack/)**: Vector database
- **[Redis OM Spring](https://redis.io/docs/stack/get-started/tutorials/stack-spring/)** for ORM
- **[Docker Compose](https://docs.docker.com/compose/)** for development
- **[Bootstrap](https://getbootstrap.com/)** Frontend toolkit
- **[HTMX](https://htmx.org)** markup-driven server-side SPAs 

### Datasets

The dataset was taken from the following Kaggle links.

- [Large Dataset](https://www.kaggle.com/datasets/paramaggarwal/fashion-product-images-dataset)
- [Smaller Dataset](https://www.kaggle.com/datasets/paramaggarwal/fashion-product-images-small)


## Running the App
Before running the app, install [Docker Desktop](https://www.docker.com/products/docker-desktop/).

#### Redis Cloud (recommended)

1. [Get your Redis Cloud Database](https://app.redislabs.com/) (if needed).

2. Export Redis Endpoint Environment Variables:
    ```bash
    $ export REDIS_HOST=your-redis-host
    $ export REDIS_PORT=your-redis-port
    $ export REDIS_PASSOWRD=your-redis-password
    ```

3. Run the App:
    ```bash
    $ docker compose -f docker-cloud-redis.yml up
    ```

> The benefit of this approach is that the db will persist beyond application runs. So you can make updates and re run the app without having to provision the dataset or create another search index.

#### Redis Docker
```bash
$ docker compose -f docker-local-redis.yml up
```

### Customizing (optional)
You can use the Jupyter Notebook in the `data/` directory to create product embeddings and product metadata JSON files. Both files will end up stored in the `data/` directory and used when creating your own container.

Create your own containers using the `build.sh` script and then make sure to update the `.yml` file with the right image name.


### Using a React development env
It's typically easier to write front end code in an interactive environment, testing changes in realtime.

1. Deploy the app using steps above.
2. Install NPM packages (you may need to use `npm` to install `yarn`)
    ```bash
    $ cd gui/
    $ yarn install --no-optional
    ````
4. Use `yarn` to serve the application from your machine
    ```bash
    $ yarn start
    ```
5. Navigate to `http://localhost:3000` in a browser.

All changes to your local code will be reflected in your display in semi realtime.

### Troubleshooting
Sometimes you need to clear out some Docker cached artifacts. Run `docker system prune`, restart Docker Desktop, and try again.

Open an issue here on GitHub and we will try to be responsive to these. Additionally, please consider [contributing](CONTRIBUTING.md).

