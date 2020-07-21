package com.reactor.demo.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple3;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * @author jiaosong
 * @desc
 * @date 2020/6/7
 */
public class DemoReactorApi {

    /**
     * 利用不同的数据创建flux
     */
    private static void createFluxFromExistingData() {
        Flux<Integer> justFlux = Flux.just(1, 2, 3, 4, 5, 6);
        subscribeFlux("justFlux", justFlux);
        Flux<Integer> arrayFlux = Flux.fromArray(new Integer[]{1, 2, 3, 4, 5, 6});
        subscribeFlux("arrayFlux", arrayFlux);
        Flux<Integer> iterableFlux = Flux.fromIterable(Arrays.asList(1, 2, 3, 4, 5, 6));
        subscribeFlux("iterableFlux", iterableFlux);
        Flux<Integer> streamFlux = Flux.fromStream(Stream.of(1, 2, 3, 4, 5, 6));
        subscribeFlux("streamFlux", streamFlux);
        Flux<Integer> rangeFlux = Flux.range(1, 6);
        subscribeFlux("rangeFlux", rangeFlux);
    }

    /**
     * 同步的，逐一产生序列 SynchronousSink
     */
    private static void createFluxWithProgram() {
        Flux<Object> generateFlux = Flux.generate(() -> 2, (state, sink) -> {
            sink.next("thread name:" + Thread.currentThread().getName() + "message #:" + state);
            if (state >= 10) {
                sink.complete();
            }
            return state + 1;
        });
        generateFlux.publishOn(Schedulers.elastic());
        subscribeFlux("generateFlux", generateFlux);
    }

    private static void subscribeFlux(String fluxName, Flux<?> flux) {
        flux.doOnSubscribe(s -> System.out.println(fluxName + ": "))
                .doOnNext(e -> System.out.print(e + ", "))
                .doOnComplete(System.out::println)
                .subscribe();
    }

    /**
     * 创建mono
     */
    private static void createMonoFromExistingData() {
        Mono<Integer> justMono = Mono.just(1);
        blockMono("justMono", justMono);
    }

    /**
     * 交给其他线程去做
     */
    private static void createMonoAsync() {
        Mono callableMono = Mono.fromCallable(() -> Thread.currentThread().getName() + " @ " + LocalDateTime.now())
                .publishOn(Schedulers.elastic());
        blockMono("callableMono", callableMono);
        Mono runnableMono = Mono.fromRunnable(() -> System.out.println(Thread.currentThread().getName() + " @ " + LocalDateTime.now()))
                .publishOn(Schedulers.elastic());
        blockMono("runnableMono", runnableMono);
        Mono supplierMono = Mono.fromSupplier(() -> Thread.currentThread().getName() + " @ " + LocalDateTime.now())
                .publishOn(Schedulers.elastic());
        blockMono("supplierMono", supplierMono);
    }


    private static void blockMono(String varName, Mono<?> mono) {
       mono.doOnSubscribe(s -> System.out.print(varName + ": "))
                .doOnNext(e -> System.out.println(e + ", "))
                .block();
    }

    /**
     * 利用flux和mono可以做如下操作
     * 转换 （Transforming）操作符负责对序列中的元素进行转变。
     * 过滤 （Filtering）操作符负责将不需要的数据从序列中进行过滤。
     * 组合 （Combining） 操作符负责将序列中的元素进行合并和连接。
     * 条件 （Conditional） 操作符负责根据特定条件对序列中的元素进行处理。
     * 数学 （Mathematical） 操作符负责对序列中的元素执行各种数学操作。
     * Obserable工具（Utility） 操作符提供的是一些针对流失处理的辅助性工具。
     * 日志和调试（Log&Debug） 操作符提供了针对运行时日志以及如何对序列进行代码调试的工具类。
     *
     */


    /**
     * 转换
     */
    private static void mapVsFlatMap() {
        Flux<String> mapFlux = Flux.just(1, 2, 3).map(i -> "id # " + i);
        subscribeFlux("mapFlux", mapFlux);
        Flux flatMapFlux = Flux.just(1, 2, 3).flatMap(i -> Flux.just("id # " + i));
        subscribeFlux("flatMap", flatMapFlux);
    }

    /**
     * 过滤
     *
     * @param
     */
    private static void useFilter() {
        Flux<Integer> filterFlux = Flux.just(1, 2, 3, 4, 5, 6).filter(i -> i > 3);
        subscribeFlux("filterFlux", filterFlux);
    }

    /**
     * 组合
     */
    private static void useThenFlow() {
        Mono thenMono = Mono.just("world")
                .map(n -> "hello " + n)
                .doOnNext(System.out::println)
                .thenReturn("do something else");
        blockMono("thenMono", thenMono);

        Flux<Integer> mergeFlux = Flux.merge(Flux.range(1, 3), Flux.range(4, 3));
        subscribeFlux("mergeFlux", mergeFlux);
    }

    /**
     * mono与flux的转换
     */
    private static void monoFluxInterchange() {
        Flux monoFlux = Mono.just(1).flux();
        subscribeFlux("monoFlux", monoFlux);
        Mono fluxMono = Flux.just(1, 2, 3).collectList();
        blockMono("fluxMono", fluxMono);
    }

    /**
     * zip
     */
    private static void zipMonoOrFlux() {
        String userName = "rust";
        Mono.fromSupplier(() -> {
            return 1;
        });
        Mono<String> profileMono = Mono.just(userName + "信息");
        Mono<String> orderMono = Mono.just(userName + "的订单");
        Mono<String> latestReviewMono = Mono.just(userName + "最近浏览");
        Mono<Tuple3<String, String, String>> zipMono = Mono.zip(profileMono, orderMono, latestReviewMono)
                .doOnNext(t -> System.out.printf("%s的主页，%s, %s, %s%n", userName, t.getT1(), t.getT2(), t.getT3()));
        blockMono("zipMono", zipMono);
    }

    public static void main(String[] args) {
//        createFluxFromExistingData();
//        System.out.println("----------");
//        createFluxWithProgram();
//        createMonoAsync();
//        createMonoFromExistingData();
//        mapVsFlatMap();
//        useFilter();
//        useThenFlow();
//        monoFluxInterchange();
//        zipMonoOrFlux();

        String content = "<html lang='zh-CN'><head><meta charset='utf-8'>"
                + "</head><body>老师好,您在双师管理后台下载的子班讲次出勤数据已经处理完成，请您<a href='%s'>点此</a>进行下载。（链接有效期为一天，过期后请您重新去平台操作下载）"
                + "</body></html>";
        String url = "https://shuangshi-excel-1255817909.cos.ap-beijing.myqcloud.com/folder/dev/%E5%AD%90%E7%8F%AD%E6%B1%87%E6%80%BB%E6%95%B0%E6%8D%AE_20200628212011_123.xlsx?sign=q-sign-algorithm%3Dsha1%26q-ak%3DAKIDl8G9mS862xiDSU7fPt2wZxUlmY3IqnBI%26q-sign-time%3D1593494646%3B1593498246%26q-key-time%3D1593494646%3B1593498246%26q-header-list%3D%26q-url-param-list%3D%26q-signature%3D45fecef6c05da9a0dd0385cdae162787f271775c";
        String format = String.format(content, url);
    }


}
