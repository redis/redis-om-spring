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

The app can run with images from a CDN (slower to vectorize) or with local images
that can be obtained from https://www.dropbox.com/s/9o59z8zbhknnmvx/product-images.zip?dl=0

Unzip the file `product-images.zip` under `src/main/resources/static/` which will 
result in the folder `src/main/resources/static/product-images` being created.

#### Redis Cloud (recommended)

1. [Get your Redis Cloud Database](https://app.redislabs.com/) (if needed).

2. Set Redis Endpoint Environment Variables (in `applications.properties`):
    ```
    spring.data.redis.host=xxxx.ec2.cloud.redislabs.com
    spring.data.redis.port=10422
    spring.data.redis.password=xxxxxx
    spring.data.redis.username=default
    ```
3. Configure whether to use local images or CDN images and how many images to 
   load, the maximum being `3000` (in `applications.properties`):
   ```
   com.redis.om.vss.useLocalImages=false
   com.redis.om.vss.maxLines=300
   ```
4. Run the App:
    ```bash
    ./mvnw install -Dmaven.test.skip && ./mvnw spring-boot:run -pl demos/roms-vss
    ```