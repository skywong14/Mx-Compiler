; ModuleID = 'builtin.c'
source_filename = "builtin.c"
target datalayout = "e-m:e-p:32:32-i64:64-n32-S128"
target triple = "riscv32-unknown-unknown-elf"

@.str = private unnamed_addr constant [3 x i8] c"%s\00", align 1
@.str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@.str.2 = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str.3 = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1

; Function Attrs: nounwind
define dso_local void @print(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str, ptr noundef %0) #11
  ret void
}

declare dso_local i32 @printf(ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local void @println(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.1, ptr noundef %0) #11
  ret void
}

; Function Attrs: nounwind
define dso_local void @printInt(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.2, i32 noundef %0) #11
  ret void
}

; Function Attrs: nounwind
define dso_local void @printlnInt(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.3, i32 noundef %0) #11
  ret void
}

; Function Attrs: nofree nounwind
define dso_local ptr @getString() local_unnamed_addr #2 {
  %1 = tail call dereferenceable_or_null(256) ptr @malloc(i32 noundef 256) #12
  %2 = tail call i32 (ptr, ...) @scanf(ptr noundef nonnull @.str, ptr noundef %1) #12
  ret ptr %1
}

; Function Attrs: argmemonly mustprogress nocallback nofree nosync nounwind willreturn
declare void @llvm.lifetime.start.p0(i64 immarg, ptr nocapture) #3

; Function Attrs: inaccessiblememonly mustprogress nofree nounwind willreturn allockind("alloc,uninitialized") allocsize(0)
declare dso_local noalias noundef ptr @malloc(i32 noundef) local_unnamed_addr #4

; Function Attrs: nofree nounwind
declare dso_local noundef i32 @scanf(ptr nocapture noundef readonly, ...) local_unnamed_addr #5

; Function Attrs: argmemonly mustprogress nocallback nofree nosync nounwind willreturn
declare void @llvm.lifetime.end.p0(i64 immarg, ptr nocapture) #3

; Function Attrs: nofree nounwind
define dso_local i32 @getInt() local_unnamed_addr #2 {
  %1 = alloca i32, align 4
  call void @llvm.lifetime.start.p0(i64 4, ptr nonnull %1) #13
  %2 = call i32 (ptr, ...) @scanf(ptr noundef nonnull @.str.2, ptr noundef nonnull %1) #12
  %3 = load i32, ptr %1, align 4, !tbaa !4
  call void @llvm.lifetime.end.p0(i64 4, ptr nonnull %1) #13
  ret i32 %3
}

; Function Attrs: nofree nounwind
define dso_local noalias ptr @toString(i32 noundef %0) local_unnamed_addr #2 {
  %2 = tail call dereferenceable_or_null(16) ptr @malloc(i32 noundef 16) #12
  %3 = tail call i32 (ptr, ptr, ...) @sprintf(ptr noundef nonnull dereferenceable(1) %2, ptr noundef nonnull @.str.2, i32 noundef %0) #12
  ret ptr %2
}

; Function Attrs: nofree nounwind
declare dso_local noundef i32 @sprintf(ptr noalias nocapture noundef writeonly, ptr nocapture noundef readonly, ...) local_unnamed_addr #5

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
define dso_local i32 @string.length(ptr nocapture noundef readonly %0) local_unnamed_addr #6 {
  %2 = tail call i32 @strlen(ptr noundef nonnull dereferenceable(1) %0) #12
  ret i32 %2
}

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
declare dso_local i32 @strlen(ptr nocapture noundef) local_unnamed_addr #7

; Function Attrs: nounwind
define dso_local ptr @string.substring(ptr noundef %0, i32 noundef %1, i32 noundef %2) local_unnamed_addr #0 {
  %4 = sub nsw i32 %2, %1
  %5 = add nsw i32 %4, 1
  %6 = tail call ptr @malloc(i32 noundef %5) #12
  %7 = getelementptr inbounds i8, ptr %0, i32 %1
  %8 = tail call ptr @memcpy(ptr noundef %6, ptr noundef %7, i32 noundef %4) #11
  %9 = getelementptr inbounds i8, ptr %6, i32 %4
  store i8 0, ptr %9, align 1, !tbaa !8
  ret ptr %6
}

declare dso_local ptr @memcpy(ptr noundef, ptr noundef, i32 noundef) local_unnamed_addr #1

; Function Attrs: nofree nounwind
define dso_local i32 @string.parseInt(ptr nocapture noundef readonly %0) local_unnamed_addr #2 {
  %2 = alloca i32, align 4
  call void @llvm.lifetime.start.p0(i64 4, ptr nonnull %2) #13
  %3 = call i32 (ptr, ptr, ...) @sscanf(ptr noundef %0, ptr noundef nonnull @.str.2, ptr noundef nonnull %2) #12
  %4 = load i32, ptr %2, align 4, !tbaa !4
  call void @llvm.lifetime.end.p0(i64 4, ptr nonnull %2) #13
  ret i32 %4
}

; Function Attrs: nofree nounwind
declare dso_local noundef i32 @sscanf(ptr nocapture noundef readonly, ptr nocapture noundef readonly, ...) local_unnamed_addr #5

; Function Attrs: argmemonly mustprogress nofree norecurse nosync nounwind readonly willreturn
define dso_local i32 @string.ord(ptr nocapture noundef readonly %0, i32 noundef %1) local_unnamed_addr #8 {
  %3 = getelementptr inbounds i8, ptr %0, i32 %1
  %4 = load i8, ptr %3, align 1, !tbaa !8
  %5 = zext i8 %4 to i32
  ret i32 %5
}

; Function Attrs: nounwind
define dso_local ptr @string.add(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strlen(ptr noundef nonnull dereferenceable(1) %0) #12
  %4 = tail call i32 @strlen(ptr noundef nonnull dereferenceable(1) %1) #12
  %5 = add nsw i32 %4, %3
  %6 = add nsw i32 %5, 1
  %7 = tail call ptr @malloc(i32 noundef %6) #12
  %8 = tail call ptr @memcpy(ptr noundef %7, ptr noundef %0, i32 noundef %3) #11
  %9 = getelementptr inbounds i8, ptr %7, i32 %3
  %10 = tail call ptr @memcpy(ptr noundef %9, ptr noundef %1, i32 noundef %4) #11
  %11 = getelementptr inbounds i8, ptr %7, i32 %5
  store i8 0, ptr %11, align 1, !tbaa !8
  ret ptr %7
}

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
define dso_local zeroext i1 @string.equal(ptr nocapture noundef readonly %0, ptr nocapture noundef readonly %1) local_unnamed_addr #6 {
  %3 = tail call i32 @strcmp(ptr noundef nonnull dereferenceable(1) %0, ptr noundef nonnull dereferenceable(1) %1) #12
  %4 = icmp eq i32 %3, 0
  ret i1 %4
}

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
declare dso_local i32 @strcmp(ptr nocapture noundef, ptr nocapture noundef) local_unnamed_addr #7

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
define dso_local zeroext i1 @string.notEqual(ptr nocapture noundef readonly %0, ptr nocapture noundef readonly %1) local_unnamed_addr #6 {
  %3 = tail call i32 @strcmp(ptr noundef nonnull dereferenceable(1) %0, ptr noundef nonnull dereferenceable(1) %1) #12
  %4 = icmp ne i32 %3, 0
  ret i1 %4
}

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
define dso_local zeroext i1 @string.less(ptr nocapture noundef readonly %0, ptr nocapture noundef readonly %1) local_unnamed_addr #6 {
  %3 = tail call i32 @strcmp(ptr noundef nonnull dereferenceable(1) %0, ptr noundef nonnull dereferenceable(1) %1) #12
  %4 = icmp slt i32 %3, 0
  ret i1 %4
}

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
define dso_local zeroext i1 @string.lessOrEqual(ptr nocapture noundef readonly %0, ptr nocapture noundef readonly %1) local_unnamed_addr #6 {
  %3 = tail call i32 @strcmp(ptr noundef nonnull dereferenceable(1) %0, ptr noundef nonnull dereferenceable(1) %1) #12
  %4 = icmp slt i32 %3, 1
  ret i1 %4
}

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
define dso_local zeroext i1 @string.greater(ptr nocapture noundef readonly %0, ptr nocapture noundef readonly %1) local_unnamed_addr #6 {
  %3 = tail call i32 @strcmp(ptr noundef nonnull dereferenceable(1) %0, ptr noundef nonnull dereferenceable(1) %1) #12
  %4 = icmp sgt i32 %3, 0
  ret i1 %4
}

; Function Attrs: argmemonly mustprogress nofree nounwind readonly willreturn
define dso_local zeroext i1 @string.greaterOrEqual(ptr nocapture noundef readonly %0, ptr nocapture noundef readonly %1) local_unnamed_addr #6 {
  %3 = tail call i32 @strcmp(ptr noundef nonnull dereferenceable(1) %0, ptr noundef nonnull dereferenceable(1) %1) #12
  %4 = icmp sgt i32 %3, -1
  ret i1 %4
}

; Function Attrs: argmemonly mustprogress nofree norecurse nosync nounwind readonly willreturn
define dso_local i32 @array.size(ptr nocapture noundef readonly %0) local_unnamed_addr #8 {
  %2 = getelementptr inbounds i8, ptr %0, i32 -4
  %3 = load i32, ptr %2, align 4, !tbaa !4
  ret i32 %3
}

; Function Attrs: mustprogress nofree nounwind willreturn
define dso_local noalias ptr @_malloc(i32 noundef %0) local_unnamed_addr #9 {
  %2 = shl nsw i32 %0, 2
  %3 = tail call ptr @malloc(i32 noundef %2) #12
  ret ptr %3
}

; Function Attrs: mustprogress nofree nounwind willreturn
define dso_local noalias nonnull ptr @__arrayMalloc__(i32 noundef %0) local_unnamed_addr #9 {
  %2 = shl i32 %0, 2
  %3 = add i32 %2, 4
  %4 = tail call ptr @malloc(i32 noundef %3) #12
  store i32 %0, ptr %4, align 4, !tbaa !4
  %5 = getelementptr inbounds i32, ptr %4, i32 1
  ret ptr %5
}

; Function Attrs: nounwind
define dso_local ptr @__arrayDeepCopy__(ptr noundef %0, i32 noundef %1) local_unnamed_addr #0 {
  %3 = icmp eq ptr %0, null
  br i1 %3, label %26, label %4

4:                                                ; preds = %2
  %5 = getelementptr inbounds i8, ptr %0, i32 -4
  %6 = load i32, ptr %5, align 4, !tbaa !4
  %7 = icmp eq i32 %1, 1
  br i1 %7, label %12, label %8

8:                                                ; preds = %4
  %9 = icmp sgt i32 %6, 0
  br i1 %9, label %10, label %26

10:                                               ; preds = %8
  %11 = add nsw i32 %1, -1
  br label %18

12:                                               ; preds = %4
  %13 = shl i32 %6, 2
  %14 = add i32 %13, 4
  %15 = tail call ptr @malloc(i32 noundef %14) #12
  store i32 %6, ptr %15, align 4, !tbaa !4
  %16 = getelementptr inbounds i32, ptr %15, i32 1
  %17 = tail call ptr @memcpy(ptr noundef nonnull %16, ptr noundef nonnull %0, i32 noundef %13) #11
  br label %26

18:                                               ; preds = %10, %18
  %19 = phi i32 [ 0, %10 ], [ %24, %18 ]
  %20 = getelementptr inbounds i32, ptr %0, i32 %19
  %21 = load i32, ptr %20, align 4, !tbaa !4
  %22 = inttoptr i32 %21 to ptr
  %23 = tail call ptr @__arrayDeepCopy__(ptr noundef %22, i32 noundef %11) #12
  %24 = add nuw nsw i32 %19, 1
  %25 = icmp eq i32 %24, %6
  br i1 %25, label %26, label %18, !llvm.loop !9

26:                                               ; preds = %18, %8, %12, %2
  %27 = phi ptr [ null, %2 ], [ %16, %12 ], [ undef, %8 ], [ undef, %18 ]
  ret ptr %27
}

; Function Attrs: mustprogress nofree norecurse nosync nounwind readnone willreturn
define dso_local zeroext i1 @__ptrEqual__(ptr noundef readnone %0, ptr noundef readnone %1) local_unnamed_addr #10 {
  %3 = icmp eq ptr %0, %1
  ret i1 %3
}

; Function Attrs: mustprogress nofree norecurse nosync nounwind readnone willreturn
define dso_local zeroext i1 @__ptrNotEqual__(ptr noundef readnone %0, ptr noundef readnone %1) local_unnamed_addr #10 {
  %3 = icmp ne ptr %0, %1
  ret i1 %3
}

attributes #0 = { nounwind "frame-pointer"="none" "min-legal-vector-width"="0" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #1 = { "frame-pointer"="none" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #2 = { nofree nounwind "frame-pointer"="none" "min-legal-vector-width"="0" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #3 = { argmemonly mustprogress nocallback nofree nosync nounwind willreturn }
attributes #4 = { inaccessiblememonly mustprogress nofree nounwind willreturn allockind("alloc,uninitialized") allocsize(0) "alloc-family"="malloc" "frame-pointer"="none" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #5 = { nofree nounwind "frame-pointer"="none" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #6 = { argmemonly mustprogress nofree nounwind readonly willreturn "frame-pointer"="none" "min-legal-vector-width"="0" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #7 = { argmemonly mustprogress nofree nounwind readonly willreturn "frame-pointer"="none" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #8 = { argmemonly mustprogress nofree norecurse nosync nounwind readonly willreturn "frame-pointer"="none" "min-legal-vector-width"="0" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #9 = { mustprogress nofree nounwind willreturn "frame-pointer"="none" "min-legal-vector-width"="0" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #10 = { mustprogress nofree norecurse nosync nounwind readnone willreturn "frame-pointer"="none" "min-legal-vector-width"="0" "no-builtin-memcpy" "no-builtin-printf" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-features"="+a,+c,+m,+relax,-save-restore" }
attributes #11 = { nobuiltin nounwind "no-builtin-memcpy" "no-builtin-printf" }
attributes #12 = { "no-builtin-memcpy" "no-builtin-printf" }
attributes #13 = { nounwind }

!llvm.module.flags = !{!0, !1, !2}
!llvm.ident = !{!3}

!0 = !{i32 1, !"wchar_size", i32 4}
!1 = !{i32 1, !"target-abi", !"ilp32"}
!2 = !{i32 1, !"SmallDataLimit", i32 8}
!3 = !{!"Ubuntu clang version 15.0.7"}
!4 = !{!5, !5, i64 0}
!5 = !{!"int", !6, i64 0}
!6 = !{!"omnipotent char", !7, i64 0}
!7 = !{!"Simple C/C++ TBAA"}
!8 = !{!6, !6, i64 0}
!9 = distinct !{!9, !10}
!10 = !{!"llvm.loop.mustprogress"}

; -------------------------

define ptr @__allocateArray__(i32 %arraySize) {
    ; 4 Byte + arraySize * 4 Byte
    %totalSize = add i32 %arraySize, 1
    %totalBytes = mul i32 %totalSize, 4

    %ptr = call ptr @_malloc(i32 %totalBytes) ; 调用malloc分配内存

    call void @llvm.memset.p0.p0.i32(ptr %ptr, i32 0, i32 %totalBytes, i1 false) ; 初始化为0 why wrong?(link error)

    ; 将数组大小存储在前4个字节
    %sizePtr = bitcast ptr %ptr to ptr  ; 将指针转换为指向i32的指针类型以存储大小
    store i32 %arraySize, ptr %sizePtr  ; 将数组大小存储到数组头部

    %elementPtr = getelementptr i32, ptr %sizePtr, i32 1 ; 跳过存储size的4个字节

    ret ptr %elementPtr ; 返回指向第一个元素的指针
}