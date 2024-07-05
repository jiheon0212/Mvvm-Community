## Images

## Description
- login button 클릭 시 bottom navigation view로 구성된 fragment로 navigate하며, navController를 교체해준다.
- 해당 fragment에서 bottom tab을 통해 navigation graph 내의 fragment 이동 가능하다.
- logout button 클릭 시 login button을 포함하는 초기 fragment를 포함하는 navController로 교체해준다.
- 리스트 뷰의 포커스를 가장 최근 메세지인 최하단에 포커스를 준다. recyclerview.scrollto 사용
- adapter를 구성할 때 viewholder를 두개 만들어서 sender, recevier로 분기해 각 viewholder가 다른 방향에서 메세지를 보여준다.
