// 테스트 JVM 의 힙을 일부러 작게(64MB) 제한합니다. 메모리를 아끼지 않는 코드는 여기서 OutOfMemoryError 로 드러납니다.
tasks.withType<Test>().configureEach {
    maxHeapSize = "64m"
}
