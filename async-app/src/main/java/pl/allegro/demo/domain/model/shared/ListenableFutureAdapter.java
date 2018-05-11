package pl.allegro.demo.domain.model.shared;

import io.reactivex.Single;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.util.concurrent.ListenableFuture;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ListenableFutureAdapter {

    public static <T> Single<T> toSingle(ListenableFuture<T> listenableFuture) {
        return Single.defer(() -> Single.create(singleEmitter -> listenableFuture.addCallback(singleEmitter::onSuccess, singleEmitter::onError)));
    }
}
