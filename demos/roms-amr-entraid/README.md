# Seamless Authentication for Azure Managed Redis using Redis OM with Microsoft Entra ID

Redis OM now provides built-in support for **Microsoft Entra ID** authentication when connecting to **Azure Managed Redis (AMR)**. This enhancement allows developers to leverage Azure's secure identity management capabilities, offering an efficient, modern, and secure authentication experience.

With Entra ID integration, your Spring Boot applications can securely connect to AMR instances without managing traditional authentication credentials, simplifying both development and operational workflows.

## Easy Configuration Steps

To utilize Microsoft Entra ID authentication in your Redis OM application, follow these straightforward configuration steps:

### Step 1: Configure AMR Host and Port

Define your Azure Managed Redis host and port in the `application.properties` file of your Spring Boot project:

```properties
spring.data.redis.host=my-amr-cluster.italynorth.redis.azure.net
spring.data.redis.port=10000
```

### Step 2: Activate Entra ID Authentication

Enable Microsoft Entra ID authentication support by adding this configuration line:

```properties
redis.om.spring.authentication.entra-id.enabled=true
```

### Step 3: Retrieve and Configure Your Azure Tenant ID

To authenticate correctly, you must specify your Azure tenant ID.

If the Azure CLI is installed on your system, you can quickly find your tenant ID using:

```bash
az account show
```

Look for the `tenantId` property in the returned JSON. For example:

```json
"tenantId": "1234abcd-56ef-0000-0000-fe65dcba4321"
```

Once you have the tenant ID, add it to your `application.properties` file as follows:

```properties
redis.om.spring.authentication.entra-id.tenant-id=1234abcd-56ef-0000-0000-fe65dcba4321
```

Make sure you're using the tenant ID that matches the environment your AMR instance is running in.

## How It Works Under the Hood

Upon startup, Redis OM automatically initializes a Redis connection using your Microsoft Entra ID credentials, seamlessly authenticating your application with Azure Managed Redis.

## Benefits of Using Microsoft Entra ID with Redis OM

- **Enhanced Security**: Utilize robust identity management and secure authentication provided by Microsoft Entra ID.
- **Simplified Management**: Eliminate the overhead of manually handling Redis access keys.
- **Improved Productivity**: Accelerate development and deployment with straightforward, standardized authentication workflows.

You're now ready to enjoy secure and hassle-free Redis connectivity powered by Redis OM and Microsoft Entra ID.