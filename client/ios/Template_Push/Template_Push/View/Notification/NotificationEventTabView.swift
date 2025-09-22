//
//  NotiView.swift
//  Template
//
//  Created by HyeongyuIM on 5/26/25.
//

import SwiftUI

import Matrix_Mobile

struct NotificationEventTabView: View {
    @StateObject var notiVM: NotiViewModel = NotiViewModel()

    @State private var selectedTab: Tab = .notification

    enum Tab {
        case notification
        case event
    }

    var body: some View {
        VStack(spacing: 0) {
            // 상단 탭 선택기 (Picker 사용)
            Picker("탭 선택", selection: $selectedTab) {
                Text("공지").tag(Tab.notification)
                Text("이벤트").tag(Tab.event)
            }
            .pickerStyle(SegmentedPickerStyle()) // 세그먼트 스타일
            .padding(.horizontal)
            .padding(.bottom, 8) // 탭과 내용 사이 간격
            .onChange(of: selectedTab, perform: { newTab in
                print("탭 변경")
                notiVM.loadDBData()
            })
            // 선택된 탭에 따라 내용 표시
            // SwiftUI 3.0 이상 (iOS 15+)에서는 if/else 또는 switch로 Content를 분기할 수 있음
            // 이전 버전에서는 Group 또는 AnyView를 사용해야 할 수 있음
            TabView(selection: $selectedTab) {
                NotificationListView(viewModel: self.notiVM)
                    .tag(Tab.notification)
                
                EventListView(viewModel: self.notiVM)
                    .tag(Tab.event)
            }
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never)) // 페이징 스타일, 인디케이터 숨김
            .animation(.easeInOut, value: selectedTab) // 탭 전환 애니메이션 (선택 사항)
            
        }
        .ignoresSafeArea(edges: .bottom)
        .navigationTitle("알림 및 이벤트") // 필요시 네비게이션 타이틀
        .task {
            self.notiVM.viewDidLoad()
        }
        .alert(isPresented: $notiVM.showPushAgreementAlert) {
            Alert(title: Text("푸시 수신에 동의 하시겠습니까?"),
                  primaryButton: .default(Text("확인"), action: {
                self.notiVM.pushNotificationAgreement(bool: true)
            }),
                  secondaryButton: .cancel(Text("거부"), action: {
                self.notiVM.pushNotificationAgreement(bool: false)
            }))
        }
    }
}

struct NotificationListView: View {
    @ObservedObject var viewModel: NotiViewModel
    // let onItemClick: (PushItem) -> Void // 필요시 클릭 핸들러

    var body: some View {
        if viewModel.notiModel.isEmpty {
            EmptyContentView(message: "표시할 공지사항이 없습니다.")
        } else {
            List {
                ForEach(viewModel.notiModel) { notification in
                    PushItemRow(item: notification)
                        // .onTapGesture { onItemClick(notification) } // 클릭 이벤트 처리
                        // 또는 NavigationLink로 상세 화면 이동
                        // NavigationLink(destination: NotificationDetailView(notification: notification)) {
                        //     PushItemRow(item: notification)
                        // }
                }
            }
            .listStyle(PlainListStyle()) // 목록 스타일
        }
    }
}

struct EventListView: View {
    @ObservedObject var viewModel: NotiViewModel
    // let onItemClick: (PushItem) -> Void // 필요시 클릭 핸들러

    var body: some View {
        if viewModel.eventModel.isEmpty {
            EmptyContentView(message: "표시할 이벤트가 없습니다.")
        } else {
            List {
                ForEach(viewModel.eventModel) { event in
                    PushItemRow(item: event)
                        // .onTapGesture { onItemClick(event) }
                        // NavigationLink(destination: EventDetailView(event: event)) {
                        //     PushItemRow(item: event)
                        // }
                }
            }
            .listStyle(PlainListStyle())
        }
    }
}


struct PushItemRow: View {
    let item: PushItem

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            // 이미지 (imageUrl이 있는 경우)
            if let imageUrlString = item.imageUrl, let url = URL(string: imageUrlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView() // 로딩 중
                            .frame(width: 80, height: 80)
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill) // 또는 .fit
                            .frame(width: 80, height: 80)
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    case .failure:
                        Image(systemName: "photo") // 에러 시 기본 이미지
                            .resizable()
                            .scaledToFit()
                            .frame(width: 80, height: 80)
                            .foregroundColor(.gray)
                            .background(Color.gray.opacity(0.1))
                            .clipShape(RoundedRectangle(cornerRadius: 8))
                    @unknown default:
                        EmptyView()
                    }
                }
                .frame(width: 80, height: 80)
            }

            // 텍스트 내용
            VStack(alignment: .leading, spacing: 4) {
                Text(item.title ?? "제목 없음")
                    .font(.headline) // titleMedium, fontWeight.Bold 느낌
                    .lineLimit(1)
                Text(item.body ?? "내용 없음")
                    .font(.subheadline) // bodyMedium 느낌
                    .lineLimit(2)
                    .foregroundColor(.gray)
                Text(item.clientStatus ?? "")
                    .font(.subheadline) // bodyMedium 느낌
                    .lineLimit(2)
                    .foregroundColor(.gray)
            }
            Spacer() // 우측 정렬을 위해
        }
        .padding(.vertical, 8)
    }
}

struct EmptyContentView: View {
    let message: String
    var body: some View {
        VStack {
            Spacer()
            Text(message)
                .font(.body)
                .foregroundColor(.secondary)
            Spacer()
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
