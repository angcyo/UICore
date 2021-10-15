# Android Sdk 下载

`sdk`环境配置`ANDROID_SDK_ROOT`

```
export ANDROID_SDK_ROOT=/angcyo/android/sdk
export $ANDROID_SDK_ROOT/platform-tools:$PATH
```

## 1: 安装`sdkmanager`

通过 https://developer.android.google.cn/studio

找到 `Command line tools only` 下载对应系统的`sdkmanager`工具.

Platform|SDK tools package|	Size|	SHA-256 checksum
--------|-----------------|-----|-------------------
Windows	|[commandlinetools-win-7583922_latest.zip](https://dl.google.com/android/repository/commandlinetools-win-7583922_latest.zip)|	104 MB	|f9e6f91743bcb1cc6905648ca751bc33975b0dd11b50d691c2085d025514278c
Mac	|[commandlinetools-mac-7583922_latest.zip](https://dl.google.com/android/repository/commandlinetools-mac-7583922_latest.zip)|	104 MB	|6929a1957f3e71008adfade0cebd08ebea9b9f506aa77f1849c7bdc3418df7cf
Linux|[commandlinetools-linux-7583922_latest.zip](https://dl.google.com/android/repository/commandlinetools-linux-7583922_latest.zip)|	104 MB|	124f2d5115eee365df6cf3228ffbca6fc3911d16f8025bebd5b1c6e2fcfa7faf

## 2: 安装`platform-tools`

```
sdkmanager "platforms;android-30" --sdk_root=/angcyo/android/sdk
```

```
sdkmanager "platform-tools" --sdk_root=/angcyo/android/sdk
```

## 列出可用软件包

```
sdkmanager --list [options]
```

## 更新软件包

```
sdkmanager --update [options]
```

`sdkmanager` 命令帮助

https://developer.android.google.cn/studio/command-line/sdkmanager