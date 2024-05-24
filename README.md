![Nsma VPN banner](/image/nsmavpn_banner.png)

# Nsma VPN
Access to freedom in the internet world only with one switch! Tap and wait until you find a proper server of hundreds of servers provided by [VPN Gate](https://www.vpngate.net/en/), without worrying about government censorship.

It is enough to sign in with a **Gmail** account that is subscribed to the [VPN Gate Daily mirror site service](http://109.111.243.206:17579/en/mail.aspx), the app will do the rest and wait for its boss for a brief hint!

This VPN uses the implementation of [SSTP](https://en.wikipedia.org/wiki/Secure_Socket_Tunneling_Protocol) protocol in the [Open SSTP Client](https://github.com/kittoku/Open-SSTP-Client) with slight changes.

## 💡 Features
- Easy to use only two steps, sign in then tap, and finally only one tap!
- There's no server for collecting and processing data, all stuff remains locally for better privacy.
- Turn on VPN, without launching the app by defining a [Quick Tile Settings](https://developer.android.com/develop/ui/views/quicksettings-tiles) in the notification drawer.
- Allows you to split the tunnel for apps you don't want to use with the VPN traffic.
- Supports two languages **English** and **Persian**.
- Optimized for all types of screen sizes.

## 🎨 UI Design
The app's design system is based on [Material 3](https://m3.material.io/), and inspired by designs for VPN apps by [Emmanuel Edokpa](https://dribbble.com/shots/16222592-TrueVPN-Mobile-App-Design/attachments/8084727?mode=media) and [Mohammad Reza Farahzad](https://dribbble.com/shots/14840886/attachments/6550059?mode=media), and by adding my own creativity, it is optimized for both dark and light modes.

## 📷 Previews
![Screenshots](/image/screenshots.png)

## 🏫 What I learned
1. How to write a **Gradle task** to sync the Detekt configs between CLI and GUI interfaces.
2. How to pipeline a basic **CI/CD** for releasing a version to the app's repository page by creating a Git tag.
3. How to write a VpnService and use **Bound Service** in practice and real-world use cases.
4. How to **ping** a VPN server by the correct way.
5. How to use **Protocol Buffer** as a serializer mechanism for **DataStore**.
6. How to use the **Google Sign-In** mechanism for authentication and authorization.
7. How to **benchmark** Jetpack Compose element for better performance.
8. How to write **Visual Test** for Jetpack Compose element animation.
9. How to **web scraping** with CSS selectors.
10. How to define a route as **Conditional Navigation** in Compose.

## 🏗️ Architecture
![Architecture scheme](/image/architecture.png)

*Undoubtedly, based on the [recommended architecture](https://developer.android.com/topic/architecture) of the official Android site*

## 📚 Tech Stack
- Jetpack Compose
- AndroidX Work Manager
- Dagger Hilt
- AndroidX DataStore
- [Protocol Buffer](https://protobuf.dev/)
- [Google API Client](https://developers.google.com/api-client-library/java)
- [Play Service Auth](https://developers.google.com/android/guides/overview)
- [Skrape{it}](https://github.com/skrapeit/skrape.it)
- [Cache4k](https://github.com/ReactiveCircus/cache4k)
- OkHttp
- **Testing**
  - AndroidX Macrobenchmark
  - [Mock Web Server](https://github.com/square/okhttp/tree/master/mockwebserver)
  - Truth Assertion
  - Robolectric

## 🛠️ How To Build
Just follow the steps of [this link](https://developers.google.com/identity/sign-in/android/legacy-gsi-start#configure-a-google-api-console-project) to get the `client id` and if you have created it for the debug key, replace it in this line:
https://github.com/ErfanSn/NsmaVPN/blob/78382f8896de6d4950dc95314a5834b5113f9491/app/build.gradle.kts#L68
otherwise:
https://github.com/ErfanSn/NsmaVPN/blob/78382f8896de6d4950dc95314a5834b5113f9491/app/build.gradle.kts#L77
And finally sync then build the project, that's it.

## 🔮 Future Plans
- [ ] Migrate the project to [KMP](https://kotlinlang.org/docs/multiplatform.html) technology, especially for Windows Desktop platform support.

## 🤝 Contributing
We welcome your contribution with open arms. Please refer to [CONTRIBUTING.md](/CONTRIBUTING.md) for instructions on how to contribute.

## 🔏 Privacy Policy
You can read about it [here](/PRIVACY_POLICY.md).

## 📜 License
**Nsma VPN** is distributed under the terms of the Apache License (Version 2.0).
See the [license](/LICENSE) for more information.
