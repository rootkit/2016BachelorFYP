################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/stasm/MOD_1/facedet.cpp \
../jni/stasm/MOD_1/initasm.cpp 

OBJS += \
./jni/stasm/MOD_1/facedet.o \
./jni/stasm/MOD_1/initasm.o 

CPP_DEPS += \
./jni/stasm/MOD_1/facedet.d \
./jni/stasm/MOD_1/initasm.d 


# Each subdirectory must supply rules for building sources it contributes
jni/stasm/MOD_1/%.o: ../jni/stasm/MOD_1/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I"D:\soft\openCV4android\OpenCV-2.4.9-android-sdk\sdk\native\jni\include" -I"D:\soft\android\NDK\android-ndk-r10d/platforms/android-21/arch-arm/usr/include" -I"D:\soft\android\NDK\android-ndk-r10d/sources/cxx-stl/gnu-libstdc++/4.9/include" -I"D:\soft\android\NDK\android-ndk-r10d/sources/cxx-stl/gnu-libstdc++/4.9/libs/armeabi-v7a/include" -O2 -g -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


