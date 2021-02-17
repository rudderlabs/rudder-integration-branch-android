# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## Integrating Branch with RudderStack's Android SDK

1. Add [BranchIO](https://branch.io) as a destination in the [Dashboard](https://app.rudderlabs.com/) and define ```branchKey```

2. Add these lines to your ```app/build.gradle```

```
repositories {
  maven {
    maven { url "https://dl.bintray.com/rudderstack/rudderstack" }
  }
}
```
3. Add the dependency under ```dependencies```

```
implementation 'com.rudderstack.android.sdk:core:1.0.1'
implementation 'com.rudderstack.android.integration:branch:0.1.3'

// branch SDK requirements
implementation 'io.branch.sdk.android:library:4.3.2'
implementation'com.android.installreferrer:installreferrer:1.1.2'
implementation 'com.google.firebase:firebase-appindexing:19.1.0'
implementation 'com.google.android.gms:play-services-ads:16+'
```

## Initialize ```RudderClient```
```
val rudderClient: RudderClient = RudderClient.getInstance(
    this,
    <WRITE_KEY>,
    RudderConfig.Builder()
        .withDataPlaneUrl(<DATA_PLANE_URL>)
        .withLogLevel(RudderLogger.RudderLogLevel.DEBUG)
        .withFactory(BranchIntegrationFactory.FACTORY)
        .build()
)
```

## Send Events

Follow the steps from the [RudderStack Android SDK](https://github.com/rudderlabs/rudder-sdk-android).

## Contact Us

If you come across any issues while configuring or using this integration, please feel free to start a conversation on our [Slack](https://resources.rudderstack.com/join-rudderstack-slack) channel. We will be happy to help you.
