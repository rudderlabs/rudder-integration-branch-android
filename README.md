# What is Rudder?

**Short answer:** 
Rudder is an open-source Segment alternative written in Go, built for the enterprise. .

**Long answer:** 
Rudder is a platform for collecting, storing and routing customer event data to dozens of tools. Rudder is open-source, can run in your cloud environment (AWS, GCP, Azure or even your data-centre) and provides a powerful transformation framework to process your event data on the fly.

Released under [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

## Getting Started with BranchIO Integration of Android SDK
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
Follow the steps from [Rudder Android SDK](https://github.com/rudderlabs/rudder-sdk-android)

## Contact Us
If you come across any issues while configuring or using RudderStack, please feel free to [contact us](https://rudderstack.com/contact/) or start a conversation on our [Discord](https://discordapp.com/invite/xNEdEGw) channel. We will be happy to help you.