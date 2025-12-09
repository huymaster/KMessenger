# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.github.huymaster.textguardian.core.api.type.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn reactor.blockhound.integration.BlockHoundIntegration
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.RuntimeMXBean
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowoptimization,allowshrinking,allowobfuscation class kotlin.coroutines.Continuation
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-keep,allowoptimization,allowshrinking,allowobfuscation class retrofit2.Response
-keep class com.firebase.** { *; }
-keep class org.apache.** { *; }
-keepnames class com.fasterxml.jackson.** { *; }
-keepnames class javax.servlet.** { *; }
-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontwarn org.shaded.apache.**
-dontwarn org.ietf.jgss.**
-keep class com.sun.jna.** { *; }
-keep class * extends com.sun.jna.Library { *; }

-keepattributes *Annotation*

-keep class kotlin.Metadata { *; }
-keep,allowobfuscation,allowshrinking class **.*$kotlin_metadata { *; }

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.flow.**
-keepnames class kotlinx.coroutines.channels.**

-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <fields>;
}
-keepclassmembers @androidx.compose.runtime.Stable class * { *; }

-keep class androidx.biometric.BiometricPrompt$CryptoObject { *; }
-keep class androidx.biometric.** { *; }

-keep class com.github.huymaster.textguardian.android.service.MessageService { *; }
-keep class com.github.huymaster.textguardian.android.data.repository.** { *; }

-keep class org.koin.core.registry.ScopeRegistry { *; }
-keep class org.koin.core.instance.InstanceFactory { *; }
-keep class org.koin.core.module.** { *; }
-keep class org.koin.core.scope.** { *; }
-keep class org.koin.core.context.** { *; }

-keep class * extends org.koin.core.module.Module
-keep class * implements org.koin.core.module.Module

-keep class org.koin.core.qualifier.StringQualifier { *; }

-keep,includedescriptorclasses class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

-dontwarn java.awt.**
-dontwarn java.beans.**
-dontnote java.awt.**
-dontnote java.beans.**
-dontwarn org.ktorm.entity.Entity$Factory
-dontwarn org.ktorm.entity.Entity
-dontwarn org.ktorm.schema.SqlType
-dontwarn javax.sql.rowset.serial.SQLInputImpl
-dontwarn javax.sql.rowset.serial.SerialArray
-dontwarn javax.sql.rowset.serial.SerialBlob
-dontwarn javax.sql.rowset.serial.SerialClob
-dontwarn javax.sql.rowset.serial.SerialRef
-dontwarn javax.sql.rowset.serial.SerialStruct
-dontwarn org.springframework.dao.DataAccessException
-dontwarn org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
-dontwarn org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator

-keepnames class com.fasterxml.jackson.databind.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.databind.**

-keep class * {
    @com.fasterxml.jackson.annotation.JsonCreator *;
    @com.fasterxml.jackson.annotation.JsonProperty *;
}

-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

-keep class io.ktor.client.engine.okhttp.** { *; }
-keep class io.ktor.client.plugins.** { *; }
-keep class io.ktor.serialization.jackson.** { *; }
-keep class io.ktor.events.** { *; }
-keep class io.ktor.websocket.** { *; }

-keep class io.ktor.client.plugins.contentnegotiation.** { *; }

-dontwarn okhttp3.**
-dontwarn okio.**
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-keep class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keep class kotlinx.coroutines.android.AndroidExceptionPreHandler { *; }
-keep class kotlinx.coroutines.android.AndroidDispatcherFactory { *; }