git diff 7a6075810faadde94271a42c472d0f7d7626335e |grep -P '^\+.*import jakarta\.\w+'| sed -r 's/^\+.*(import +jakarta.[^.]+).*$/\1/' |sort | uniq

P=$(git diff 7a6075810faadde94271a42c472d0f7d7626335e |grep -P '^\+.*import jakarta\.\w+'| sed -r 's/^\+.*(import +jakarta.[^.]+).*$/\1/' |sort | uniq | sed 's/import jakarta.//')

for p in $P; do 
	echo PROCESSING: $p
	git diff 7a6075810faadde94271a42c472d0f7d7626335e |grep -P -- '^-.*import +.*'$p | sed -r 's/^\-.*(import +.*'$p')\..*$/\1/' |sort | uniq
done
