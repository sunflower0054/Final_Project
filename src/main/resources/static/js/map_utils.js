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

                // 병원 리스트 생성 (search.html 완벽 동일 스타일)
                var li = document.createElement('li');
                li.className = 'facility-item';
                li.innerHTML = `
                    <div class="f-name">${place.place_name}</div>
                    <div class="f-dist">집에서 ${place.distance}m 떨어짐</div>
                    <div class="f-info">전화: ${place.phone || '정보 없음'}</div>
                    <div class="f-info">주소: ${place.address_name}</div>
                `;

                // 리스트 아이템 클릭 시 지도 이동 & 말풍선 표시
                li.onclick = (function(overlay, pos) {
                    return function() {
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