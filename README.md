# What is RudderStack?

[RudderStack](https://rudderstack.com/) is a **customer data pipeline** tool for collecting, routing and processing data from your websites, apps, cloud tools, and data warehouse.

More information on RudderStack can be found [here](https://github.com/rudderlabs/rudder-server).

## Integrating Branch with RudderStack's Android SDK

1. Add [BranchIO](https://branch.io) as a destination in the [Dashboard](https://app.rudderlabs.com/) and define ```branchKey```

2. Add the dependency under ```dependencies```
```
implementation 'com.rudderstack.android.sdk:core:[1.20.1, 2.0.0)'
implementation 'com.rudderstack.android.integration:branch:1.0.0'
```

3. Add the following optional dependencies needed by Branch under ```dependencies```

```
// required if your app is in the Google Play Store (tip: avoid using bundled play services libs)
implementation 'com.google.android.gms:play-services-ads-identifier:17.1.0+'
// alternatively, use the following lib for getting the AAID
// implementation 'com.google.android.gms:play-services-ads:17.2.0'
// optional
// Chrome Tab matching (enables 100% guaranteed matching based on cookies)
implementation 'androidx.browser:browser:1.0.0'
// Replace above with the line below if you do not support androidx
// implementation 'com.android.support:customtabs:28.0.0'
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
