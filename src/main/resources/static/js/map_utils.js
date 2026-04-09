// 전역 변수로 현재 열린 커스텀 오버레이(말풍선) 저장
let currentCustomOverlay = null;

// 1. 지도 초기화 및 거주자 마커(별 모양 + 남색 라벨) 찍기
function initMapAndHospitals(targetAddress, targetName) {
    kakao.maps.load(function () {
        var mapContainer = document.getElementById('map');
        var mapOption = { center: new kakao.maps.LatLng(37.566826, 126.978656), level: 5 };
        var map = new kakao.maps.Map(mapContainer, mapOption);
        var geocoder = new kakao.maps.services.Geocoder();

        if (targetAddress) {
            geocoder.addressSearch(targetAddress, function(result, status) {
                if (status === kakao.maps.services.Status.OK) {
                    var coords = new kakao.maps.LatLng(result[0].y, result[0].x);
                    map.setCenter(coords);

                    // 거주자 마커 (search.html과 동일한 별 마커)
                    var imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png';
                    var imageSize = new kakao.maps.Size(24, 35);
                    var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize);

                    new kakao.maps.Marker({
                        position: coords,
                        map: map,
                        image: markerImage
                    });

                    // 거주자 남색 라벨 (CustomOverlay)
                    new kakao.maps.CustomOverlay({
                        position: coords,
                        content: `<div class="resident-label">🏠 ${targetName}님의 집</div>`,
                        yAnchor: 2.3
                    }).setMap(map);

                    // 주변 응급실 검색 실행
                    searchHospitals(coords, map);
                } else {
                    document.getElementById('hospital-list').innerHTML = '<li style="padding:20px; text-align:center; color:var(--error);">주소를 찾을 수 없습니다.</li>';
                }
            });
        }
    });
}

// 2. 주변 응급실 검색 및 리스트 & 커스텀 말풍선 생성
function searchHospitals(center, map) {
    var ps = new kakao.maps.services.Places();
    var hospitalListEl = document.getElementById('hospital-list');

    ps.keywordSearch('응급실', function(data, status) {
        if (status === kakao.maps.services.Status.OK) {
            hospitalListEl.innerHTML = '';

            // 기존 불필요한 장소 필터링
            var filteredData = data.filter(function(place) {
                var name = place.place_name;
                return !name.includes('동물') && !name.includes('요양') &&
                       !name.includes('떡볶이') && !name.includes('한의원') &&
                       !name.includes('청소') && !name.includes('치과');
            });

            if (filteredData.length === 0) {
                hospitalListEl.innerHTML = '<li style="padding:20px; text-align:center; color:var(--error);">조건에 맞는 주변 응급실을 찾을 수 없습니다.</li>';
                return;
            }

            var limit = Math.min(filteredData.length, 5);

            for (var i = 0; i < limit; i++) {
                var place = filteredData[i];

                // 병원 마커 표시
                var hospitalPos = new kakao.maps.LatLng(place.y, place.x);
                var hospitalMarker = new kakao.maps.Marker({
                    position: hospitalPos,
                    map: map
                });

                // search.html 스타일 말풍선 (CustomOverlay)
                var iwContent = `
                    <div class="custom-infowindow">
                        <div class="custom-infowindow-title">${place.place_name}</div>
                        <div class="custom-infowindow-desc">${place.phone || '전화번호 정보 없음'}</div>
                    </div>
                `;

                var customOverlay = new kakao.maps.CustomOverlay({
                    position: hospitalPos,
                    content: iwContent,
                    yAnchor: 1.2
                });

                // 마커 클릭 시 말풍선 열기
                kakao.maps.event.addListener(hospitalMarker, 'click', (function(overlay, pos) {
                    return function() {
                        if (currentCustomOverlay) currentCustomOverlay.setMap(null);
                        overlay.setMap(map);
                        currentCustomOverlay = overlay;
                        map.panTo(pos);
                    };
                })(customOverlay, hospitalPos));

                // 병원 리스트 생성 (CSS 구조에 맞게 수정 + 전화버튼 부활)
                var phoneText = place.phone || '전화번호 없음';
                var phoneLink = place.phone ? 'tel:' + place.phone.replace(/-/g, '') : '#';

                var li = document.createElement('li');
                li.className = 'facility-item';
                li.innerHTML = `
                    <div class="f-info-wrap">
                        <div class="f-name">${place.place_name}</div>
                        <div class="f-dist">집에서 ${place.distance}m 떨어짐</div>
                        <div class="f-info">${place.road_address_name || place.address_name}</div>
                    </div>
                    <a href="${phoneLink}" class="h-call-btn" style="flex-shrink: 0; z-index: 10;">
                        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="margin-right:4px;"><path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/></svg>
                        ${phoneText}
                    </a>
                `;

                // 리스트 아이템 클릭 시 지도 이동 & 말풍선 표시
                li.onclick = (function(overlay, pos) {
                    return function(e) {
                        // 전화 버튼(a 태그) 클릭 시에는 지도 이동 무시하고 전화만 걸리게 예외 처리
                        if(e.target.closest('.h-call-btn')) return;

                        map.panTo(pos);
                        if (currentCustomOverlay) currentCustomOverlay.setMap(null);
                        overlay.setMap(map);
                        currentCustomOverlay = overlay;
                    };
                })(customOverlay, hospitalPos);

                hospitalListEl.appendChild(li);
            }
        } else {
            hospitalListEl.innerHTML = '<li style="padding:20px; text-align:center; color:var(--error);">주변 응급실 정보를 찾을 수 없습니다.</li>';
        }
    }, {
        location: center,
        radius: 5000,
        sort: kakao.maps.services.SortBy.DISTANCE,
    });
}